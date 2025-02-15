package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long>, JpaSpecificationExecutor<Ledger> {

    @Query("select l from Ledger l where l.code = :code")
    Optional<Ledger> findLedgerByCode(@Param("code") String code);
}
