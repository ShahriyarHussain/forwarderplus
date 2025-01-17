package com.lazoft.forwarderplus.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipmentStatus {
    NEW("New"),
    SHIPPING_ORDER_CREATED("Shipping Order OK"),
    SHIPMENT_ADVICE_DONE("Shipment Advice OK"),
    SHIPMENT_ADVICE_SENT("Shipment Advice Sent"),
    SHIPPING_INSTRUCTION_SUBMITTED("SI Submitted"),
    BL_DRAFT_SENT_TO_SHIPPER("B/L Draft Sent"),
    BL_CONFIRMED("Final B/L Confirmed"),
    BL_RELEASED("B/L Release"),
    INVOICE_SENT_TO_SHIPPER("ShipmentInvoice Sent To Shipper"),
    SHIPMENT_COMPLETE("Shipment Complete");

    private final String status;

}
