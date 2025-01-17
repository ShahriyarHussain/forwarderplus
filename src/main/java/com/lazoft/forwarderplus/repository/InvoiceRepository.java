package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.ShipmentInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<ShipmentInvoice, String> {

    @Query("select i from ShipmentInvoice i where i.shipmentId = :shipmentId")
    ShipmentInvoice getInvoiceByShipmentId(@Param("shipmentId") long shipmentId);

}
