package com.lazoft.forwarderplus.views.export.order;

import com.lazoft.forwarderplus.dto.xml.CustomItem;
import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Carrier;
import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.util.CustomItemUtil;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Shipping Order")
@Route(value = "shipping-order", layout = MainLayout.class)
@RolesAllowed({"EXPORT", "ADMIN"})
@Uses(Icon.class)
public class ShippingOrderView extends Div {

    private final ShipmentService shipmentService;
    private final AuthenticatedUser authenticatedUser;
    private final ClientService clientService;
    private final UserService userService;
    private Grid<Shipment> grid;

    private final Filters filters;

    public ShippingOrderView(PortService portService, ShipmentService shipmentService, CarrierService carrierService,
                             AuthenticatedUser authenticatedUser, ClientService clientService, UserService userService) {
        this.shipmentService = shipmentService;
        this.authenticatedUser = authenticatedUser;
        this.clientService = clientService;
        this.userService = userService;

        setSizeFull();
        addClassNames("view-shipments-view");
        filters = new Filters(this::refreshGrid, portService, carrierService);
        VerticalLayout layout = new VerticalLayout(filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    public static class Filters extends Div implements Specification<Shipment> {

        private final TextField bookingNo = new TextField("Booking No");
        private final ComboBox<Port> portOfLoading = new ComboBox<>("Loading Port");
        private final ComboBox<Port> portOfDestination = new ComboBox<>("Destination Port");
        private final ComboBox<Carrier> carrier = new ComboBox<>("Carrier");
        private final ComboBox<String> commodity = new ComboBox<>("Commodity");
        private final ComboBox<ContainerSize> containerSize = new ComboBox<>("Container Size");
        private final DatePicker createFromDate = new DatePicker("Created Date");
        private final DatePicker createdToDate = new DatePicker();

        public Filters(Runnable onSearch, PortService portService, CarrierService carrierService) {
            List<Port> ports = portService.getAllPorts();
            List<Carrier> carriers = carrierService.getAllCarriers();

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            bookingNo.setPlaceholder("Booking No");
            bookingNo.addKeyDownListener(keyDownEvent -> {
                if (keyDownEvent.getKey() == Key.ENTER || keyDownEvent.getKey() == Key.NUMPAD_ENTER) {
                    onSearch.run();
                }
            });

            containerSize.setItems(ContainerSize.values());
            containerSize.setItemLabelGenerator(ContainerSize::getContainerSize);

            commodity.setItems(CustomItemUtil.getItemsListFromFile("commodities").stream().map(CustomItem::getName).toList());
            carrier.setItems(carriers);
            carrier.setItemLabelGenerator(Carrier::getName);

            portOfLoading.setItems(ports);
            portOfLoading.setItemLabelGenerator(Port::getPortLabel);
            portOfDestination.setItems(ports);
            portOfDestination.setItemLabelGenerator(Port::getPortLabel);

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                bookingNo.clear();
                createFromDate.clear();
                createdToDate.clear();
                portOfLoading.setValue(portOfLoading.getEmptyValue());
                portOfDestination.setValue(portOfDestination.getEmptyValue());
                containerSize.setValue(containerSize.getEmptyValue());
                carrier.clear();
                commodity.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(bookingNo, portOfLoading, portOfDestination, commodity, carrier, containerSize, createDateFilter(), actions);
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
            root.fetch("schedule", JoinType.LEFT);

            if (!bookingNo.isEmpty()) {
                String bookingNoLowerCase = bookingNo.getValue().toLowerCase();
                Join<Shipment, Booking> bookingJoin = root.join("booking");
                Predicate bookingNoMatch = criteriaBuilder.like(criteriaBuilder.lower(
                        bookingJoin.get("bookingNo")), "%" + bookingNoLowerCase + "%");
                predicates.add(bookingNoMatch);
            }
            if (!portOfLoading.isEmpty()) {
                long portId = portOfLoading.getValue().getId();
                Join<Shipment, Booking> bookingJoin = root.join("booking");
                Join<Booking, Port> portJoin = bookingJoin.join("loadingPort");
                Predicate portMatch = criteriaBuilder.equal(portJoin.get("id"), portId);
                predicates.add(portMatch);
            }
            if (!portOfDestination.isEmpty()) {
                long portId = portOfDestination.getValue().getId();
                Join<Shipment, Booking> bookingJoin = root.join("booking");
                Join<Booking, Port> portJoin = bookingJoin.join("destinationPort");
                Predicate portMatch = criteriaBuilder.equal(portJoin.get("id"), portId);
                predicates.add(portMatch);
            }
            if (!carrier.isEmpty()) {
                long carrierId = carrier.getValue().getId();
                Join<Shipment, Carrier> carrierJoin = root.join("carrier");
                Predicate carrierMatch = criteriaBuilder.equal(carrierJoin.get("id"),  carrierId);
                predicates.add(carrierMatch);
            }
            if (!commodity.isEmpty()) {
                String lowerCaseFilter = commodity.getValue().toLowerCase();
                Predicate commodityMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("commodity")), "%" + lowerCaseFilter + "%");
                predicates.add(commodityMatch);
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
                Predicate containerSizeMatch = criteriaBuilder.equal(bookingJoin.get("containerSize"), containerSize.getValue());
                predicates.add(containerSizeMatch);
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
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
        grid.addComponentColumn(shipment -> {
            Span statusBadge = new Span(shipment.getStatus().getStatus());
            statusBadge.getElement().getThemeList().add("badge");
            statusBadge.getStyle().setBackgroundColor(shipment.getStatus().getColor());
            statusBadge.getStyle().setColor("beige");
            statusBadge.getStyle().set("font-weight", "bold");
            return statusBadge;
        }).setHeader("Status").setAutoWidth(true).setSortable(true);
        grid.addColumn(shipment -> shipment.getCreatedBy().getUsername()).setHeader("Created By").setAutoWidth(true);
        grid.addColumn(shipment -> shipment.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy 'T' hh:mm:ss")))
                .setHeader("Created On").setAutoWidth(true).setSortable(true);
        grid.addComponentColumn(this::getCreateShippingOrderButton).setTextAlign(ColumnTextAlign.CENTER)
                .setHeader("Shipping Order").setAutoWidth(true);
        grid.addItemDoubleClickListener(event -> getCreateShippingOrderButton(event.getItem()).click());

        grid.setItems(query -> shipmentService.getShipmentsByFilter(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    public void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private Button getCreateShippingOrderButton(Shipment shipment) {
        Button create = new Button(LineAwesomeIcon.PLUS_SOLID.create());
        create.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        create.addClickListener(event -> new ShippingOrderDialog(
                shipment, authenticatedUser, clientService, shipmentService, userService, this).open());
        return create;
    }
}
