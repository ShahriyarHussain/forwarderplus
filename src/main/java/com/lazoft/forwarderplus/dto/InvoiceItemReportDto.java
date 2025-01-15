package com.lazoft.forwarderplus.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InvoiceItemReportDto {
    private long slNo;
    private String description;
    private String quantityWithUnit;
    private String rate;
    private String totalInForeignCurr;
    private String subtotal;

    @Override
    public String toString() {
        return slNo + " - " + description + " - " + quantityWithUnit + " - " + rate + " - " + totalInForeignCurr + " - " + subtotal;
    }
}