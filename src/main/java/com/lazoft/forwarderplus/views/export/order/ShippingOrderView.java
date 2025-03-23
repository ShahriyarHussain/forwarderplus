package com.lazoft.forwarderplus.views.export.order;

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

    private final ShipmentFilter filters;

    public ShippingOrderView(PortService portService, ShipmentService shipmentService, CarrierService carrierService,
                             AuthenticatedUser authenticatedUser, ClientService clientService, UserService userService) {
        this.shipmentService = shipmentService;
        this.authenticatedUser = authenticatedUser;
        this.clientService = clientService;
        this.userService = userService;

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
        grid.addComponentColumn(this::getCreateShippingOrderButton).setTextAlign(ColumnTextAlign.CENTER)
                .setHeader("Edit Order").setAutoWidth(true);
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
        Button create = new Button(LineAwesomeIcon.PEN_SOLID.create());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        create.addClickListener(event -> new ShippingOrderDialog(
                shipment, authenticatedUser, clientService, shipmentService, userService, this).open());
        return create;
    }
}
