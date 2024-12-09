package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    public List<Shipment> getAll() {return shipmentRepository.findAll();}

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

    @EntityGraph("shipment")
    public Page<Shipment> getShipmentsByFilter(Pageable pageable, Specification<Shipment> filter) {
        return shipmentRepository.findAll(filter, pageable);
    }

    public Set<Shipment> createShipmentFromBooking(Booking booking) {
        Set<Shipment> shipments = new HashSet<>();
        for (int i = 0; i < booking.getNumOfShipments(); i++) {
            Shipment shipment = new Shipment();
            shipment.setCreatedOn(LocalDateTime.now());
            shipment.setCreatedBy(booking.getCreatedBy());
            shipment.setBooking(booking);
            shipments.add(shipment);
            shipment.setShipper(booking.getShipper());
            shipment.setCarrier(booking.getCarrier());
            shipment.setNumOfContainers(booking.getNumOfContainers() / booking.getNumOfShipments());
            shipment.setStatus(ShipmentStatus.NEW);
        }
        return saveAllShipments(shipments);
    }
}
