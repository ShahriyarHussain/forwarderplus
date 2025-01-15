package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, String> {
}
