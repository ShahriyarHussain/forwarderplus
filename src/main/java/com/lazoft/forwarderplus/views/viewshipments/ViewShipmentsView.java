package com.lazoft.forwarderplus.views.viewshipments;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.services.PortService;
import com.lazoft.forwarderplus.services.ShipmentService;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
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
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@PageTitle("View Shipments")
@Route(value = "view-shipments", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ViewShipmentsView extends Div {

    private final ShipmentService shipmentService;
    private Grid<Shipment> grid;

    private final Filters filters;

    public ViewShipmentsView(PortService portService, ShipmentService shipmentService) {
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
        private final MultiSelectComboBox<String> shipper = new MultiSelectComboBox<>("Shipper");
        private final ComboBox<Port> portOfLoading = new ComboBox<>("Loading Port");
        private final ComboBox<Port> portOfDestination = new ComboBox<>("Destination Port");
        private final Select<ShipmentStatus> status = new Select<>();
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

            portOfLoading.setItems(ports);
            portOfLoading.setItemLabelGenerator(Port::getPortLabel);

            portOfDestination.setItems(ports);
            portOfDestination.setItemLabelGenerator(Port::getPortLabel);

            status.setItems(ShipmentStatus.values());
            status.setLabel("Shipment Status");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                bookingNo.clear();
                blNo.clear();
                createFromDate.clear();
                createdToDate.clear();
                shipper.clear();
                portOfLoading.setValue(portOfLoading.getEmptyValue());
                portOfDestination.setValue(portOfDestination.getEmptyValue());
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(bookingNo, blNo, portOfLoading, portOfDestination, shipper, status, createDateFilter(), actions);
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
//            if (!shipper.isEmpty()) {
//                String databaseColumn = "occupation";
//                List<Predicate> occupationPredicates = new ArrayList<>();
//                for (String occupation : shipper.getValue()) {
//                    occupationPredicates
//                            .ad.d(criteriaBuilder.equal(criteriaBuilder.literal(occupation), root.get(databaseColumn)));
//                }
//                predicates.add(criteriaBuilder.or(occupationPredicates.toArray(Predicate[]::new)));
//            }
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
            return booking.getLoadingPort().getPortCityAndCountry() + " -> " + booking.getDestinationPort().getPortCityAndCountry();
        }).setHeader("Route").setAutoWidth(true).setSortable(false);
        grid.addColumn("status").setAutoWidth(true).setSortable(true);
        grid.addColumn(shipment -> shipment.getCreatedBy().getUsername()).setHeader("Created By").setAutoWidth(true);
        grid.addColumn(shipment -> shipment.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy 'T' hh:mm:ss")))
                .setHeader("Created On").setAutoWidth(true).setSortable(true);

        grid.setItems(query -> shipmentService.getShipmentsByFilter(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
//        grid.setItems(shipmentService.getAll());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
