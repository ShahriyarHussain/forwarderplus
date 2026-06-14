package com.lazoft.forwarderplus.entity.finance;

import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.exception.ValidationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Transaction")
public class Transaction extends AccountingBaseEntity {
    private LocalDate date;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TransactionLeg> transactionLegs = new HashSet<>();

    @PrePersist
    public void validateTransactionLegs() {
        if (!transactionLegs.stream()
                .map(TransactionLeg::getCreditAmount).reduce(BigDecimal.ZERO, BigDecimal::add).equals(
                transactionLegs.stream()
                        .map(TransactionLeg::getDebitAmount).reduce(BigDecimal.ZERO, BigDecimal::add))) {
            throw new ValidationException("Debit Amount and Credit Amount does not match");
        }
    }

    public void addLegs(Collection<TransactionLeg> legs) {
        legs.forEach(transactionLeg -> {
            transactionLeg.setTransaction(this);
            transactionLegs.add(transactionLeg);
        });
    }
}
