package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.AccountType;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "accNoIdx", columnList = "accountNo", unique = true))
public class Account extends AbstractEntity {

    private String name;
    private String accountNo;
    private AccountType accountType;
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;
    private AmountCurrency amountCurrency;
    private String financialDetails;
}
