package com.lazoft.forwarderplus.repository.finance;

import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLegRepository extends
        JpaRepository<TransactionLeg, Long>,
        JpaSpecificationExecutor<TransactionLeg>,
        TransactionLegQueryRepository {
}
