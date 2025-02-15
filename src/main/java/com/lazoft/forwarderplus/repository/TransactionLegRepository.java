package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.TransactionLeg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLegRepository extends JpaRepository<TransactionLeg, Long> {
}
