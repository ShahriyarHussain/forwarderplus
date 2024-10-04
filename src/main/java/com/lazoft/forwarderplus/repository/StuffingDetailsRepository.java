package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.entity.StuffingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
//
@Repository
public interface StuffingDetailsRepository extends JpaRepository<StuffingDetails, Shipment> {
}
