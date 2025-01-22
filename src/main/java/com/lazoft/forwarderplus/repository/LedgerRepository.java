package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    @Query("select count(l) from Ledger l where l.code = :code")
    long countLedgerByCode(@Param("code") String code);
}
