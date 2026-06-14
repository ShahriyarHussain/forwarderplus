package com.lazoft.forwarderplus.entity.finance;

import com.lazoft.forwarderplus.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "Account")
@Entity
public class Account extends AccountingBaseEntity {
    private String name;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private String remarks;
}
