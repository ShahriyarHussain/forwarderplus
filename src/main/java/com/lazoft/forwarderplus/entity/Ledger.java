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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgenerator")
    @SequenceGenerator(name = "idgenerator", initialValue = 1000)
    private Long ledgerId;
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
}
