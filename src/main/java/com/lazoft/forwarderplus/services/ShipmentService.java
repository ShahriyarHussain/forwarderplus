package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    public Shipment saveShipment(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    public Set<Shipment> saveAllShipments(Set<Shipment> shipments) {
        Set<Shipment> savedShipments = new HashSet<>();
        for (Shipment shipment : shipments) {
            savedShipments.add(saveShipment(shipment));
        }
        return savedShipments;
    }

    public void saveAll(Collection<Shipment> shipments) {
        shipmentRepository.saveAll(shipments);
    }

    public Set<Shipment> createShipmentFromBooking(Booking booking, int numOfShipments) {
        Set<Shipment> shipments = new HashSet<>();
        for (int i = 0; i < numOfShipments; i++) {
            Shipment shipment = new Shipment();
            shipment.setCreatedOn(LocalDateTime.now());
            shipment.setCreatedBy(booking.getCreatedBy());
            shipment.setBooking(booking);
            shipment.setStatus(ShipmentStatus.NEW);
            shipments.add(shipment);
            shipment.setShipper(booking.getShipper());
        }
        return saveAllShipments(shipments);
    }
}
