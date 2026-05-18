package com.lazoft.forwarderplus.service.finance.impl;

import com.lazoft.forwarderplus.dto.TransactionSummary;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.entity.finance.Transaction;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import com.lazoft.forwarderplus.enums.TransactionType;
import com.lazoft.forwarderplus.repository.finance.TransactionLegRepository;
import com.lazoft.forwarderplus.repository.finance.TransactionRepository;
import com.lazoft.forwarderplus.service.finance.TransactionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionLegRepository transactionLegRepository;
    private final EntityManager entityManager;

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Override
    public Page<TransactionLeg> getShipmentsByFilter(Pageable pageable,
                                                     Specification<TransactionLeg> filter) {
        return transactionLegRepository.findAll(filter, pageable);
    }

    public TransactionSummary getTransactionSummary(Specification<Transaction> filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<TransactionLeg> root = query.from(TransactionLeg.class);

        Expression<Long> totalCount = cb.count(root);
        Expression<BigDecimal> totalAmount = cb.coalesce(cb.sum(root.get("totalAmount")), BigDecimal.ZERO);

        // Debit-specific
//        Expression<Long> incomeCount = cb.count(cb.selectCase()
//                .when(cb.equal(root.get("type"), TransactionType.INCOME), 1));
//        Expression<BigDecimal> incomeAmount = cb.coalesce(
//                cb.sum(cb.<BigDecimal>selectCase()
//                        .when(cb.equal(root.get("type"), TransactionType.INCOME), root.get("totalAmount"))
//                        .otherwise(BigDecimal.ZERO)
//                ), BigDecimal.ZERO);
//
//        // Credit-specific
//        Expression<Long> expenseCount = cb.count(cb.selectCase()
//                .when(cb.equal(root.get("type"), TransactionType.EXPENSE), 1));
//        Expression<BigDecimal> expenseAmount = cb.coalesce(
//                cb.sum(cb.<BigDecimal>selectCase()
//                        .when(cb.equal(root.get("type"), TransactionType.EXPENSE), root.get("totalAmount"))
//                        .otherwise(BigDecimal.ZERO)
//                ), BigDecimal.ZERO);

//        query.multiselect(totalCount, totalAmount, incomeCount, incomeAmount, expenseCount, expenseAmount);

//        if (filter != null) {
//            Predicate predicate = filter.toPredicate(root, query, cb);
//            query.where(predicate);
//        }

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
}
