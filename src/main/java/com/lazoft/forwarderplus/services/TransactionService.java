package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.TransactionType;
import com.lazoft.forwarderplus.repository.TransactionLegRepository;
import com.lazoft.forwarderplus.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final TransactionLegRepository transactionLegRepository;
    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final BatchNoService batchNoService;

    @Value("${income.ledger.code}")
    private String incomeGlCode;
    @Value("${expense.ledger.code}")
    private String expenseGlCode;
    
    @Transactional
    public Transaction postTransaction(Transaction transaction) {
        List<TransactionLeg> savedTransactionLegs = transactionLegRepository.saveAll(transaction.getTransactionLegs());
        transaction.setTransactionLegs(savedTransactionLegs);
        transaction.setBatchNo(batchNoService.getBatchNoByDate(transaction.getBusinessDate()));
        Transaction savedTransaction = transactionRepository.save(transaction);
        updateBalanceOfEntitiesInTransaction(transaction);
        return savedTransaction;
    }

    private void updateBalanceOfEntitiesInTransaction(Transaction transaction) {
        if (transaction.getFromAccount() != null) {
            updateAccountBalance(transaction.getFromAccount(), transaction);
        }
        if (transaction.getFromLedger() != null) {
            updateLedgerBalance(transaction.getFromLedger(), transaction);
        }
        if (transaction.getToAccount() != null) {
            updateAccountBalance(transaction.getToAccount(), transaction);
        }
        if (transaction.getToLedger() != null) {
            updateLedgerBalance(transaction.getToLedger(), transaction);
        }
        if (transaction.getType() != TransactionType.TRANSFER ) {
            addToIncomeOrExpenseLedger(transaction.getTotalAmount(), transaction.getType());
        }
    }

    private void addToIncomeOrExpenseLedger(BigDecimal totalAmount, TransactionType type) {
        String ledgerCode = "";
        if (type == TransactionType.INCOME) {
            ledgerCode = incomeGlCode;
        } else if (type == TransactionType.EXPENSE) {
            ledgerCode = expenseGlCode;
        }
        Ledger ledger = ledgerService.getLedgerByCode(ledgerCode).orElseThrow();
        ledger.setCurrentBalance(ledger.getCurrentBalance().add(totalAmount));
        ledgerService.saveLedger(ledger);
    }

    private void updateAccountBalance(Account account, Transaction transaction) {
        BigDecimal totalAmount = transaction.getTotalAmount();
        List<Ledger> updatedLedger = updateTaggedLedgerBalanceAndGet(account.getTaggedLedgers(), transaction);
        ledgerService.saveAllLedgers(updatedLedger);
        account.setCurrentBalance(getUpdatedBalance(account.getCurrentBalance(), totalAmount, transaction.getType()));
        account.setUpdatedAt(LocalDateTime.now());
        accountService.saveOnlyAccount(account);
    }

    private List<Ledger> updateTaggedLedgerBalanceAndGet(List<LedgerTagInfo> taggedLedgers, Transaction transaction) {
        if (taggedLedgers.isEmpty()) {
            return new LinkedList<>();
        }
        List<Ledger> updatedLedger = new LinkedList<>();
        BigDecimal totalAmount = transaction.getTotalAmount();
        taggedLedgers.forEach(ledgerTagInfo -> {
            Ledger ledger = ledgerTagInfo.getLedger();
            switch (ledgerTagInfo.getTransactionType()) {
                case FOLLOW_ACC_TRAN:
                    ledger.setCurrentBalance(getUpdatedBalance(ledger.getCurrentBalance(), totalAmount, transaction.getType()));
                    break;
                case FOLLOW_REVERSE_ACC_TRAN:
                    ledger.setCurrentBalance(getUpdatedBalance(ledger.getCurrentBalance(), totalAmount, transaction.getType().getOpposite()));
                    break;
                case ONLY_ACC_CREDIT_TRAN:
                    if (transaction.getType() == TransactionType.INCOME) {
                        ledger.setCurrentBalance(getUpdatedBalance(ledger.getCurrentBalance(), totalAmount, transaction.getType()));
                    }
                    break;
                case ONLY_ACC_DEBIT_TRAN:
                    if (transaction.getType() == TransactionType.EXPENSE) {
                        ledger.setCurrentBalance(getUpdatedBalance(ledger.getCurrentBalance(), totalAmount, transaction.getType()));
                    }
                    break;
                case CHOOSE_ON_TRAN:
                    ledger.setCurrentBalance(getUpdatedBalance(ledger.getCurrentBalance(), totalAmount, transaction.getLedgerTransactionType().get(ledger.getCode())));
                    break;
                default:
                    break;
            }
            updatedLedger.add(ledger);
        });
        return updatedLedger;
    }

    private void updateLedgerBalance(Ledger ledger, Transaction transaction) {
        BigDecimal totalAmount = transaction.getTotalAmount();
        ledger.setCurrentBalance(getUpdatedBalance(ledger.getCurrentBalance(), totalAmount, transaction.getType()));
        ledgerService.saveLedger(ledger);
    }
    
    private BigDecimal getUpdatedBalance(BigDecimal currentBalance, BigDecimal amount, TransactionType transactionType) {
        if (transactionType == TransactionType.EXPENSE) {
            return currentBalance.subtract(amount);
        }
        return currentBalance.add(amount);
    }

}
