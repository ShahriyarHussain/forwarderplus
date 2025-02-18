package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String>, JpaSpecificationExecutor<Shipment> {

    @Query("select s from Shipment s where s.booking.bookingNo like :filter or s.hblNo like :filter or s.mblNo like :filter")
    List<Shipment> getShipmentsByFilter(@Param("filter") String filter);
}
