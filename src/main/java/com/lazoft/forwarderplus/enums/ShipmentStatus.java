package com.lazoft.forwarderplus.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipmentStatus {
    NEW("New", "black"),
    SHIPPING_ORDER_OK("Shipping Order OK", "darkOrange"),
    SHIPMENT_ADVICE_OK("Advice OK", "darkGreen"),
    INVOICE_OK("Invoice OK", "darkBlue"),
    SHIPMENT_ADVICE_SENT("Shipment Advice Sent", "darkSlateGrey"),
    SHIPPING_INSTRUCTION_SUBMITTED("SI Submitted", "darkSlateGrey"),
    BL_DRAFT_SENT_TO_SHIPPER("B/L Draft Sent", "darkSlateGrey"),
    BL_CONFIRMED("Final B/L Confirmed", "darkSlateGrey"),
    BL_RELEASED("B/L Release", "darkSlateGrey"),
    SHIPMENT_COMPLETE("Shipment Complete", "darkSlateGrey");

    private final String status;
    private final String color;

}
