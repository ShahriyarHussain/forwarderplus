package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AccountType;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "accNoIdx", columnList = "accountNo", unique = true))
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accidgenerator")
    @SequenceGenerator(name = "accidgenerator", initialValue = 100, allocationSize = 1)
    private long accountId;
    private String name;
    private String accountNo;
    private AccountType accountType;
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;
    private AmountCurrency amountCurrency;
    private String financialDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Version
    private long updateSl;

    @OneToMany(fetch = FetchType.EAGER)
    private List<LedgerTagInfo> taggedLedgers;
}
