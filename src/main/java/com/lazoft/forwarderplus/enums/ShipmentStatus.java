package com.lazoft.forwarderplus.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipmentStatus {
    NEW("New"),
    SHIPPING_ORDER_OK("Shipping Order OK"),
    SHIPMENT_ADVICE_OK("Advice OK"),
    INVOICE_OK("Invoice OK"),
    SHIPMENT_ADVICE_SENT("Shipment Advice Sent"),
    SHIPPING_INSTRUCTION_SUBMITTED("SI Submitted"),
    BL_DRAFT_SENT_TO_SHIPPER("B/L Draft Sent"),
    BL_CONFIRMED("Final B/L Confirmed"),
    BL_RELEASED("B/L Release"),
    SHIPMENT_COMPLETE("Shipment Complete");

    private final String status;

}
