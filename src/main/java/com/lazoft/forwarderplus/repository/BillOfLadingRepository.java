package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.BillOfLading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillOfLadingRepository extends JpaRepository<BillOfLading, Long> {
}
