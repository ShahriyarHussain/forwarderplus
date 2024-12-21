package com.lazoft.forwarderplus.views.exportviews.shipmentAdvice;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Shipment Advice")
@Route(value = "shipment-advice", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
@Uses(Icon.class)
public class ShipmentAdviceView extends Div {

    private final ShipmentService shipmentService;
    private final ScheduleService scheduleService;
    private final CarrierService carrierService;
    private final ClientService clientService;
    private final PortService portService;
    private final ContainerDetailsService containerDetailsService;

    private final AuthenticatedUser authenticatedUser;

    private Grid<Shipment> grid;

    private final Filters filters;

    public ShipmentAdviceView(PortService portService, ShipmentService shipmentService,
                              AuthenticatedUser authenticatedUser, ClientService clientService,
                              StuffingDetailsService stuffingDetailsService, ScheduleService scheduleService, CarrierService carrierService, PortService portService1, ContainerDetailsService containerDetailsService) {
        this.shipmentService = shipmentService;
        this.authenticatedUser = authenticatedUser;
        this.clientService = clientService;
        this.scheduleService = scheduleService;
        this.carrierService = carrierService;
        this.portService = portService1;
        this.containerDetailsService = containerDetailsService;

        setSizeFull();
        addClassNames("view-shipments-view");
        filters = new Filters(this::refreshGrid, portService);
        VerticalLayout layout = new VerticalLayout(filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
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
            root.fetch("schedule", JoinType.LEFT);

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
    }

    private Component createGrid() {
        grid = new Grid<>(Shipment.class, false);
        grid.getStyle().set("hover", "cursor");
        grid.addColumn(shipment -> shipment.getBooking().getBookingNo()).setHeader("Booking No").setAutoWidth(true);
        grid.addColumn("hblNo").setHeader("House B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn("mblNo").setHeader("Master B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn("clientInvoiceNo").setAutoWidth(true);
        grid.addColumn(shipment -> shipment.getShipper().getName()).setHeader("Shipper").setAutoWidth(true);
        grid.addColumn(shipment -> {
            Booking booking = shipment.getBooking();
            return booking.getLoadingPort().getPortCityAndCountry() + " - " + booking.getDestinationPort().getPortCityAndCountry();
        }).setHeader("Route").setAutoWidth(true).setSortable(false);
        grid.addColumn(shipment -> shipment.getStatus().getStatus()).setAutoWidth(true).setSortable(true);
        grid.addColumn(shipment -> shipment.getCreatedBy().getUsername()).setHeader("Created By").setAutoWidth(true);
        grid.addColumn(shipment -> shipment.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy 'T' hh:mm:ss")))
                .setHeader("Created On").setAutoWidth(true).setSortable(true);
        grid.addColumn(shipment -> shipment.getCreatedBy().getUsername()).setHeader("Created By").setAutoWidth(true);
        grid.addComponentColumn(this::getCreateButtonForShipment).setTextAlign(ColumnTextAlign.CENTER)
                .setHeader("Edit Advice").setAutoWidth(true);
        grid.addItemDoubleClickListener(event -> getCreateButtonForShipment(event.getItem()).click());

        grid.setItems(query -> shipmentService.getShipmentsByFilter(PageRequest.of(query.getPage(), query.getPageSize(),
                        VaadinSpringDataHelpers.toSpringDataSort(query)), filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private Button getCreateButtonForShipment(Shipment shipment) {
//        User user = authenticatedUser.get().orElse(shipment.getCreatedBy());
        Button create = new Button(VaadinIcon.EDIT.create());
        create.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        create.addClickListener(event -> new ShipmentAdviceDialog(shipmentService, scheduleService,
                carrierService, clientService, portService, containerDetailsService, shipment).open());
        return create;
    }


    public void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }
}