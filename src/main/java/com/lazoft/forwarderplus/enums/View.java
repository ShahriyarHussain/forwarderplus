package com.lazoft.forwarderplus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum View {
    SHIPPING_ORDER("Shipping Order"),
    SHIPMENT_ADVICE("Shipment Advice"),
    SHIPMENT_INVOICE("Shipment Invoice"),
    VIEW_SHIPMENT("View Shipments"),
    BL_MANAGER("B/L Manager"),
    ADD_BOOKING("Add Booking"),
    MANAGE_PARTIES("Add Parties"),
    ADD_OTHERS("Add Others"),
    CREATE_BOOKING("Create Booking"),
    MISC_MANAGEMENT("Misc Management"), // includes Add Carrier, Add Commodity and Add Ports
    CREATE_INVOICE("Create ShipmentInvoice");

    private final String viewName;
}
