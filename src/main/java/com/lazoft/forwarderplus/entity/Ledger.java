package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "ledgerCodeIdx", columnList = "code", unique = true))
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ledgeridgenerator")
    @SequenceGenerator(name = "ledgeridgenerator", initialValue = 100, allocationSize = 1)
    private long ledgerId;
    private String name;
    private String code;
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;
    private AmountCurrency currency;
    private String description;
    private boolean isSystemLedger;
    private LocalDateTime createdOn;
    @Version
    private LocalDateTime updatedOn;

    @Override
    public String toString() {
        return name + "-" + code + "-" + startingBalance + "-" + currentBalance;
    }
}
