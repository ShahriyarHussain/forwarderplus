package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    @Query("select i from Invoice i where i.shipmentId = :shipmentId")
    Invoice getInvoiceByShipmentId(@Param("shipmentId") long shipmentId);

}
