package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    public void saveShipment(Shipment shipment) {
        shipmentRepository.save(shipment);
    }
}
