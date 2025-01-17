package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(indexes = @Index(name = "shipmentIdIdx", columnList = "shipmentId"))
public class ShipmentInvoice {

    @Id
    private String invoiceNo;
    private long shipmentId;
    private String expNo;
    private LocalDate expDate;
    private BigDecimal conversionRate;
    private BigDecimal grandTotal;
    private AmountCurrency foreignCurrency;
    private AmountCurrency localCurrency;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<InvoiceItem> invoiceItems;
}