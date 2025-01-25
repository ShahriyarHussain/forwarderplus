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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgenerator")
    @SequenceGenerator(name = "idgenerator", initialValue = 1000)
    private long accountId;
    private String name;
    private String accountNo;
    private AccountType accountType;
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;
    private AmountCurrency amountCurrency;
    private String financialDetails;
    private LocalDateTime createdAt;
    @Version
    private LocalDateTime updatedAt;

    @OneToMany(fetch = FetchType.EAGER)
    private List<LedgerTagInfo> taggedLedgers;
}
