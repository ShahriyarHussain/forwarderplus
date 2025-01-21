package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Ledger extends AbstractEntity {

    private String name;
    private String code;
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;
    private AmountCurrency currency;
    private String description;
    private LocalDateTime createdOn;
//    @OneToOne
//    private User createdBy;
//    private LocalDateTime updatedOn;
//    @OneToOne
//    private User updatedBy;
}
