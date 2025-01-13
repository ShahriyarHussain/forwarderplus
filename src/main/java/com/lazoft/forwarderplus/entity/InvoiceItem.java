package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

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
    private BigDecimal subTotalInLocalCurr;
    @Transient
    private BigDecimal subTotalInForeignCurr;
    @Transient
    private int sl;


}
