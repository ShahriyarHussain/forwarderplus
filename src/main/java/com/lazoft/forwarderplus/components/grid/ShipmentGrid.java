package com.lazoft.forwarderplus.components.grid;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;

public class ShipmentGrid extends Grid<Shipment> {

    public ShipmentGrid() {
        addColumn(shipment -> shipment.getBooking().getBookingNo()).setHeader("Booking No").setAutoWidth(true).setSortable(true);
        addColumn("hblNo").setHeader("House B/L No").setAutoWidth(true).setSortable(false);
        addColumn("mblNo").setHeader("Master B/L No").setAutoWidth(true).setSortable(false);
        addColumn("clientInvoiceNo").setAutoWidth(true).setSortable(false);
        addColumn(shipment -> shipment.getShipper().getName()).setHeader("Shipper").setAutoWidth(true);
        addColumn(shipment -> {
            Booking booking = shipment.getBooking();
            return booking.getLoadingPort().getPortCityAndCountry() + " - " + booking.getDestinationPort().getPortCityAndCountry();
        }).setHeader("Route").setAutoWidth(true).setSortable(false);
        addComponentColumn(shipment -> {
            Span statusBadge = new Span(shipment.getStatus().getStatus());
            statusBadge.getElement().getThemeList().add("badge");
            statusBadge.getStyle().setBackgroundColor(shipment.getStatus().getColor());
            statusBadge.getStyle().setColor("beige");
            statusBadge.getStyle().set("font-weight", "bold");
            return statusBadge;
        }).setHeader("Status").setAutoWidth(true).setSortable(true);
        addColumn(shipment -> shipment.getCreatedBy().getUsername()).setHeader("Created By").setAutoWidth(true);
        addColumn(shipment -> shipment.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy 'T' hh:mm:ss")))
                .setHeader("Created On").setAutoWidth(true).setSortable(true);
        addThemeVariants(GridVariant.LUMO_NO_BORDER);
        addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    }
}
