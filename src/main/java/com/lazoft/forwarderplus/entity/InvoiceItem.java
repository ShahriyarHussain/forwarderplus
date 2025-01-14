package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class InvoiceItem {

    @Id
    private String id;

    private String description;
    private long quantity;
    private BigDecimal price;
    private String itemUnit;

    @Transient
    private boolean isForeignCurrency;
    @Transient
    private BigDecimal subTotalInLocalCurr;
    @Transient
    private BigDecimal subTotalInForeignCurr;
    @Transient
    private int sl;


}
