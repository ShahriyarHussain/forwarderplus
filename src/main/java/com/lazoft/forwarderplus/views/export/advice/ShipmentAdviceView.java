package com.lazoft.forwarderplus.views.export.advice;

import com.lazoft.forwarderplus.components.filter.ShipmentFilter;
import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.security.AuthenticatedUser;
import com.lazoft.forwarderplus.services.*;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.format.DateTimeFormatter;

@PageTitle("Shipment Advice")
@Route(value = "shipment-advice", layout = MainLayout.class)
@RolesAllowed({"EXPORT", "ADMIN"})
@Uses(Icon.class)
public class ShipmentAdviceView extends Div {

    private final ShipmentService shipmentService;
    private final ScheduleService scheduleService;
    private final CarrierService carrierService;
    private final ClientService clientService;
    private final PortService portService;
    private final UserService userService;
    private final IdGenerationService idGenerationService;

    private final AuthenticatedUser authenticatedUser;

    private Grid<Shipment> grid;

    private final ShipmentFilter filters;

    public ShipmentAdviceView(PortService portService, ShipmentService shipmentService, ClientService clientService,
                              ScheduleService scheduleService, CarrierService carrierService, UserService userService,
                              IdGenerationService idGenerationService, AuthenticatedUser authenticatedUser) {
        this.shipmentService = shipmentService;
        this.clientService = clientService;
        this.scheduleService = scheduleService;
        this.carrierService = carrierService;
        this.portService = portService;
        this.userService = userService;
        this.idGenerationService = idGenerationService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        addClassNames("view-shipments-view");
        filters = new ShipmentFilter(this::refreshGrid, portService, carrierService);
        VerticalLayout layout = new VerticalLayout(filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }


    private Component createGrid() {
        grid = new Grid<>(Shipment.class, false);
        grid.getStyle().set("hover", "cursor");
        grid.addColumn(shipment -> shipment.getBooking().getBookingNo()).setHeader("Booking No").setAutoWidth(true).setSortable(true);
        grid.addColumn("hblNo").setHeader("House B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn("mblNo").setHeader("Master B/L No").setAutoWidth(true).setSortable(false);
        grid.addColumn("clientInvoiceNo").setAutoWidth(true).setSortable(false);
        grid.addColumn(shipment -> StringUtils.truncate(shipment.getShipper().getName(), 35)).setHeader("Shipper").setAutoWidth(true);
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
        Button create = new Button(LineAwesomeIcon.PEN_SOLID.create());
        create.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        create.addClickListener(event -> new ShipmentAdviceDialog(shipmentService, scheduleService,
                carrierService, clientService, portService, userService, idGenerationService, shipment, authenticatedUser).open());
        return create;
    }

    public void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }
}