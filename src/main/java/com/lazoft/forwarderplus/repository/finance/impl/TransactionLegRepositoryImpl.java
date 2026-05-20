package com.lazoft.forwarderplus.repository.finance.impl;

import com.lazoft.forwarderplus.dto.AccountTypeSummaryDto;
import com.lazoft.forwarderplus.entity.finance.Account;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import com.lazoft.forwarderplus.repository.finance.TransactionLegQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionLegRepositoryImpl implements TransactionLegQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<AccountTypeSummaryDto> getSummariesByAccountType(Specification<TransactionLeg> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AccountTypeSummaryDto> query = cb.createQuery(AccountTypeSummaryDto.class);
        Root<TransactionLeg> root = query.from(TransactionLeg.class);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        Join<TransactionLeg, Account> accountJoin = root.join("account", JoinType.INNER);
        query.multiselect(
                accountJoin.get("accountType"),
                cb.count(root),
                cb.sum(root.get("debitAmount")),
                cb.sum(root.get("creditAmount"))
        );
        query.groupBy(accountJoin.get("accountType"));
        return em.createQuery(query).getResultList();
    }
}