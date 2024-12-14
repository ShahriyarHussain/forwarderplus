package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String>, JpaSpecificationExecutor<Shipment> {

}
