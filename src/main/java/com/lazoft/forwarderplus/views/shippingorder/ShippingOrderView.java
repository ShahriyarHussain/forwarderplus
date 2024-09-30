package com.lazoft.forwarderplus.views.shippingorder;

import com.lazoft.forwarderplus.Util.DateUtil;
import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.PackageUnit;
import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.views.MainLayout;
import com.lazoft.forwarderplus.views.viewshipments.ViewShipmentsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIcon;

@PageTitle("Shipping Order")
@Route(value = "shipping-order", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ShippingOrderView extends Div {

    private final ShipmentService shipmentService;
    private Grid<Shipment> grid;

    private final Filters filters;

    public ShippingOrderView(PortService portService, ShipmentService shipmentService) {
        this.shipmentService = shipmentService;

        setSizeFull();
        addClassNames("view-shipments-view");
        filters = new Filters(this::refreshGrid, portService);
        VerticalLayout layout = new VerticalLayout(filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<Shipment> {

        private final TextField bookingNo = new TextField("Booking No");
        private final TextField blNo = new TextField("Bill Of Lading No");
        private final ComboBox<Port> portOfLoading = new ComboBox<>("Loading Port");
        private final ComboBox<Port> portOfDestination = new ComboBox<>("Destination Port");
        private final ComboBox<ContainerSize> containerSize = new ComboBox<>("Container Size");
        private final DatePicker createFromDate = new DatePicker("Created Date");
        private final DatePicker createdToDate = new DatePicker();

        public Filters(Runnable onSearch, PortService portService) {
            List<Port> ports = portService.getAllPorts();

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            bookingNo.setPlaceholder("Booking No");
            blNo.setPlaceholder("B/L No");

            containerSize.setItems(ContainerSize.values());

            portOfLoading.setItems(ports);
            portOfLoading.setItemLabelGenerator(Port::getPortLabel);

            portOfDestination.setItems(ports);
            portOfDestination.setItemLabelGenerator(Port::getPortLabel);

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                bookingNo.clear();
                blNo.clear();
                createFromDate.clear();
                createdToDate.clear();
                portOfLoading.setValue(portOfLoading.getEmptyValue());
                portOfDestination.setValue(portOfDestination.getEmptyValue());
                containerSize.setValue(containerSize.getEmptyValue());
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(bookingNo, blNo, portOfLoading, portOfDestination, createDateFilter(), actions);
        }

        private Component createDateFilter() {
            createFromDate.setPlaceholder("From");

            createdToDate.setPlaceholder("To");

            // For screen readers
            createFromDate.setAriaLabel("Created From");
            createdToDate.setAriaLabel("Created From");

            FlexLayout portSelectionComponent = new FlexLayout(createFromDate, new Text(" – "), createdToDate);
            portSelectionComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            portSelectionComponent.addClassName(LumoUtility.Gap.XSMALL);
            return portSelectionComponent;
        }

        @Override
        public Predicate toPredicate(Root<Shipment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!bookingNo.isEmpty()) {
                String lowerCaseFilter = bookingNo.getValue().toLowerCase();
                Join<Shipment, Booking> bookingJoin = root.join("booking");
                Predicate bookingNoMatch = criteriaBuilder.equal(
                        criteriaBuilder.lower(bookingJoin.get("bookingNo")), lowerCaseFilter);
                predicates.add(bookingNoMatch);
            }
            if (!blNo.isEmpty()) {
                String lowerCaseFilter = blNo.getValue().toLowerCase();
                Predicate mblMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("mblNo")), "%" + lowerCaseFilter + "%");
                Predicate hblMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("hblNo")), "%" + lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(mblMatch, hblMatch));
            }
            if (createFromDate.getValue() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"),
                        criteriaBuilder.literal(createFromDate.getValue())));
            }
            if (createdToDate.getValue() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"),
                        criteriaBuilder.literal(createdToDate.getValue())));
            }
            if (!containerSize.isEmpty()) {
                Join<Shipment, Booking> bookingJoin = root.join("booking");
                Predicate containerSizeMatch = criteriaBuilder.equal(bookingJoin.get("containerSize"),
                        containerSize.getValue().getContainerSize());
                predicates.add(containerSizeMatch);
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private String ignoreCharacters(String characters, String in) {
            String result = in;
            for (int i = 0; i < characters.length(); i++) {
                result = result.replace("" + characters.charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
                                                    Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression,
                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(Shipment.class, false);
        grid.addColumn(shipment -> shipment.getBooking().getBookingNo()).setHeader("Booking No").setAutoWidth(true);
        grid.addColumn("hblNo").setHeader("House B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn("mblNo").setHeader("Master B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn("clientInvoiceNo").setAutoWidth(true);
        grid.addColumn(shipment -> shipment.getShipper().getName()).setHeader("Shipper").setAutoWidth(true);
        grid.addColumn(shipment -> {
            Booking booking = shipment.getBooking();
            return booking.getLoadingPort().getPortCityAndCountry() + " - " + booking.getDestinationPort().getPortCityAndCountry();
        }).setHeader("Route").setAutoWidth(true).setSortable(false);
        grid.addColumn("status").setAutoWidth(true).setSortable(true);
        grid.addColumn(shipment -> shipment.getCreatedBy().getUsername()).setHeader("Created By").setAutoWidth(true);
        grid.addColumn(shipment -> shipment.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy 'T' hh:mm:ss")))
                .setHeader("Created On").setAutoWidth(true).setSortable(true);
        grid.addComponentColumn(this::getCreateButtonForShipment).setTextAlign(ColumnTextAlign.CENTER)
                .setHeader("Shipping Order").setAutoWidth(true);

        grid.setItems(query -> shipmentService.getShipmentsByFilter(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private Button getCreateButtonForShipment(Shipment shipment) {
        Button create = new Button(LineAwesomeIcon.PLUS_SOLID.create());
        create.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        create.addClickListener(event -> new ShippingOrderPage(shipment).open());
        return create;
    }

    public static class ShippingOrderPage extends Dialog {
        private final DatePicker documentDate = new DatePicker("Report Date");
        private final TextField bookingNo = new TextField("Booking No");
        private final TextField cnfAgentName = new TextField("CnF Agent Name");
        private final TextField cnfAgentContact = new TextField("CnF Agent Contact");
        private final TextField shipper = new TextField("Shipper");
        private final TextField portOfLoading = new TextField("Port Of Loading");
        private final TextField portOfDischarge = new TextField("Port Of Discharge");
        private final TextField vessel = new TextField("Vessel");
        private final ComboBox<Client> notifyParty = new ComboBox<>("Notify Party");
        private final IntegerField quantity = new IntegerField("Quantity");
        private final ComboBox<PackageUnit> units = new ComboBox<>("Units");
        private StuffingDetails stuffingDetails;
        private final Booking booking;
        private Schedule schedule;

        public ShippingOrderPage(Shipment shipment) {
            stuffingDetails = shipment.getStuffingDetails();
            booking = shipment.getBooking();
            schedule = shipment.getSchedule();

            this.setWidth(800, Unit.PIXELS);
            FormLayout formLayout = new FormLayout();
            formLayout.add(documentDate, new Hr(), bookingNo, vessel, portOfLoading, portOfDischarge, shipper, notifyParty,
                    cnfAgentName, cnfAgentContact, quantity, units);
            formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0",2));

            setValues(shipment);

            Button saveButton = new Button("Save");
            saveButton.setIcon(LineAwesomeIcon.SAVE_SOLID.create());
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            saveButton.addClickListener(event -> {
                if (stuffingDetails == null) {
                    stuffingDetails = new StuffingDetails();
                    stuffingDetails.setQuantity(quantity.getValue());
                }
                if (schedule == null) {
                    schedule = new Schedule();
                    schedule.setFeederVesselName(vessel.getValue());
                    schedule.setPortOfLoading(booking.getLoadingPort());
                    schedule.setPortOfDischarge(booking.getDestinationPort());
                }
            });

            this.add(new H3("Shipping Order"), new Hr(), formLayout);

            Button printButton = new Button("Download As PDF");
            printButton.setIcon(LineAwesomeIcon.PRINT_SOLID.create());
            printButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

            Button closeButton = new Button("Close", event -> this.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            this.getFooter().add(saveButton, printButton, closeButton);
        }

        private void setValues(Shipment shipment) {
            documentDate.setValue(LocalDate.now());
            units.setItems(PackageUnit.values());
            bookingNo.setValue(booking.getBookingNo());
            bookingNo.setReadOnly(true);
            portOfLoading.setValue(booking.getLoadingPort().getPortLabel());
            portOfDischarge.setValue(booking.getDestinationPort().getPortLabel());
            shipper.setValue(shipment.getShipper().getName());
            shipper.setReadOnly(true);
            Client notifyParty = shipment.getNotifyParty();
            if (notifyParty != null) {
                this.notifyParty.setValue(notifyParty);
            }

            if (stuffingDetails != null) {
                cnfAgentName.setValue(stuffingDetails.getCnfAgentName());
                cnfAgentContact.setValue(stuffingDetails.getCnfAgentContactNo());
                //.setValue(stuffingDetails.getStuffingCharge());
                //.setValue(stuffingDetails.getStuffingDate());
                //.setValue(stuffingDetails.getStuffingDepot());
            }

            if (schedule != null) {
                vessel.setValue(schedule.getFeederVesselName());
                portOfLoading.setValue(schedule.getPortOfLoading().getPortLabel());
                portOfDischarge.setValue(schedule.getPortOfDischarge().getPortLabel());
            }
        }


        private Map<String, Object> prepareParamsForShippingOrder(Shipment shipment, User user) {
            Map<String, Object> paramMap = new HashMap<>();
            StuffingDetails stuffingDetails = shipment.getStuffingDetails();
            Booking booking = shipment.getBooking();
            Schedule schedule = shipment.getSchedule();


            paramMap.put("LOGO_URL", "Images/logo_best.png");

            paramMap.put("DATE", DateUtil.getCurrentDate());
            paramMap.put("BOOKING_NO", booking.getBookingNo());

            paramMap.put("CNF_AGENT", stuffingDetails.getCnfAgentName());
            paramMap.put("CONTACT", stuffingDetails.getCnfAgentContactNo());
            paramMap.put("SHIPPER_NAME", shipment.getShipper().getName());
            paramMap.put("NOTIFY_PARTY", shipment.getNotifyParty().getName());

            paramMap.put("CONTAINERS", booking.getNumOfContainers() + " X " + booking.getContainerSize());
            paramMap.put("GOODS_DESC", booking.getCommodity());
            paramMap.put("QUANTITY", stuffingDetails.getQuantity() + stuffingDetails.getPackageUnit().toString());

            paramMap.put("PORT_OF_LOADING", schedule.getPortOfLoading().getPortCityAndCountry());
            paramMap.put("VESSEL", schedule.getFeederVesselName());
            paramMap.put("PORT_OF_DELIVERY", schedule.getPortOfDischarge().getPortCityAndCountry());

            paramMap.put("SHIPPING_LINE", shipment.getCarrier());

            paramMap.put("SIGNED_BY", user.getName());
            paramMap.put("SIGNED_BY_EMAIL", user.getEmail());
            paramMap.put("SIGNED_BY_CONTACT", user.getContactNo());

            return paramMap;
        }

    }


}
