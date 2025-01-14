package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Invoice;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public Invoice getInvoiceFromShipment(Shipment shipment) {
        return invoiceRepository.getInvoiceByShipmentId(shipment.getShipmentId());
    }

    public void saveInvoice(Invoice invoice) {
        invoiceRepository.save(invoice);
    }

    public boolean isInvoiceNoExists(String invoiceNo) {
        return invoiceRepository.existsById(invoiceNo);
    }
}
