package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Transshipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransshipmentRepository extends JpaRepository<Transshipment, Long> {
}
