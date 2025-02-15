package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.BatchInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BatchInfoRepository extends JpaRepository<BatchInfo, Long> {

    Optional<BatchInfo> findByBusinessDate(LocalDate businessDate);
}
