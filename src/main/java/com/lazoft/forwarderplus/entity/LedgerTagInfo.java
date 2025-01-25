package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.LedgerTransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(indexes = @Index(name = "ledgerAccountIdx", columnList = "ledger_id, account_id"))
public class LedgerTagInfo extends AbstractEntity {
    
    @ManyToOne
    @JoinColumn(name = "ledgerId")
    private Ledger ledger;
    @ManyToOne
    @JoinColumn(name = "accountId")
    private Account account;
    private LedgerTransactionType transactionType;
}
