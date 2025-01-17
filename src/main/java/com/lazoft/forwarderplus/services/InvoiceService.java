package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.ShipmentInvoice;
import com.lazoft.forwarderplus.entity.InvoiceItem;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.repository.InvoiceItemRepository;
import com.lazoft.forwarderplus.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public ShipmentInvoice getInvoiceFromShipment(Shipment shipment) {
        return invoiceRepository.getInvoiceByShipmentId(shipment.getShipmentId());
    }

    public void saveInvoice(ShipmentInvoice shipmentInvoice) {
        List<InvoiceItem> savedItems = invoiceItemRepository.saveAll(shipmentInvoice.getInvoiceItems());
        shipmentInvoice.setInvoiceItems(savedItems);
        invoiceRepository.save(shipmentInvoice);
    }

    public boolean isInvoiceNoExists(String invoiceNo) {
        return invoiceRepository.existsById(invoiceNo);
    }
}
