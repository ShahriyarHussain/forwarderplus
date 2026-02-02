package com.lazoft.forwarderplus.services.finance.impl;

import com.lazoft.forwarderplus.dto.TransactionResult;
import com.lazoft.forwarderplus.dto.TransactionSummary;
import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.TransactionType;
import com.lazoft.forwarderplus.repository.TransactionLegRepository;
import com.lazoft.forwarderplus.repository.TransactionRepository;
import com.lazoft.forwarderplus.services.LedgerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final EntityManager entityManager;

    @Value("${income.ledger.code}")
    private String incomeGlCode;
    @Value("${expense.ledger.code}")
    private String expenseGlCode;

    @Transactional
    public int postTransaction(List<Transaction> transactionList) {
        int batchNo = batchNoService.getBatchNoByDate(transactionList.get(0).getBusinessDate());
        for (Transaction transaction : transactionList) {
            List<TransactionLeg> savedTransactionLegs = transactionLegRepository.saveAll(transaction.getTransactionLegs());
            transaction.setTransactionLegs(savedTransactionLegs);
            transaction.setBatchNo(batchNo);
            transactionRepository.save(transaction);
            updateBalanceOfEntitiesInTransaction(transaction);
        }
        return batchNo;
    }

    public TransactionSummary getTransactionSummary(Specification<Transaction> filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Transaction> root = query.from(Transaction.class);

        Expression<Long> totalCount = cb.count(root);
        Expression<BigDecimal> totalAmount = cb.coalesce(cb.sum(root.get("totalAmount")), BigDecimal.ZERO);

        // Debit-specific
        Expression<Long> incomeCount = cb.count(cb.selectCase()
                .when(cb.equal(root.get("type"), TransactionType.INCOME), 1));
        Expression<BigDecimal> incomeAmount = cb.coalesce(
                cb.sum(cb.<BigDecimal>selectCase()
                        .when(cb.equal(root.get("type"), TransactionType.INCOME), root.get("totalAmount"))
                        .otherwise(BigDecimal.ZERO)
                ), BigDecimal.ZERO);

        // Credit-specific
        Expression<Long> expenseCount = cb.count(cb.selectCase()
                .when(cb.equal(root.get("type"), TransactionType.EXPENSE), 1));
        Expression<BigDecimal> expenseAmount = cb.coalesce(
                cb.sum(cb.<BigDecimal>selectCase()
                        .when(cb.equal(root.get("type"), TransactionType.EXPENSE), root.get("totalAmount"))
                        .otherwise(BigDecimal.ZERO)
                ), BigDecimal.ZERO);

        query.multiselect(totalCount, totalAmount, incomeCount, incomeAmount, expenseCount, expenseAmount);

        if (filter != null) {
            Predicate predicate = filter.toPredicate(root, query, cb);
            query.where(predicate);
        }

        Object[] result = entityManager.createQuery(query).getSingleResult();

        TransactionSummary summary = new TransactionSummary();
        summary.setTotalCount((Long) result[0]);
        summary.setTotalAmount((BigDecimal) result[1]);
        summary.setIncomeCount((Long) result[2]);
        summary.setIncomeAmount((BigDecimal) result[3]);
        summary.setExpenseCount((Long) result[4]);
        summary.setExpenseAmount((BigDecimal) result[5]);

        return summary;
    }

    public TransactionResult getTransactionsAndSummaryByFilter(Pageable pageable, Specification<Transaction> filter) {
        Page<Transaction> page = transactionRepository.findAll(filter, pageable);
        TransactionSummary summary = getTransactionSummary(filter);

        TransactionResult result = new TransactionResult();
        result.setTransactions(page);
        result.setSummary(summary);
        return result;
    }

    private void updateBalanceOfEntitiesInTransaction(Transaction transaction) {
        if (transaction.getTransactionAccount() != null) {
            updateAccountBalance(transaction.getTransactionAccount(), transaction);
        }
        if (transaction.getTransactionLedger() != null) {
            updateLedgerBalance(transaction.getTransactionLedger(), transaction);
        }
        if (transaction.getType() != TransactionType.TRANSFER) {
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
