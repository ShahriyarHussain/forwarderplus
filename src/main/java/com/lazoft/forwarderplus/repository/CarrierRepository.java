package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CarrierRepository extends JpaRepository<Carrier, Long>, JpaSpecificationExecutor<Carrier> {

    List<Carrier> findAllByOrderByNameAsc();

}
