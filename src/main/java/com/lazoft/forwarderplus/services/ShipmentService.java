package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.*;
import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.repository.ShipmentRepository;
import com.lazoft.forwarderplus.repository.TransshipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final ContainerDetailsService containerDetailsService;
    private final ScheduleService scheduleService;
    private final TransshipmentRepository transshipmentRepository;

    public List<Shipment> getAll() {return shipmentRepository.findAll();}

    public Shipment saveShipment(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    public List<Shipment> saveAllShipments(List<Shipment> shipments) {
        List<Shipment> savedShipments = new LinkedList<>();
        for (Shipment shipment : shipments) {
            savedShipments.add(saveShipment(shipment));
        }
        return savedShipments;
    }

    @EntityGraph("shipment")
    public Page<Shipment> getShipmentsByFilter(Pageable pageable, Specification<Shipment> filter) {
        return shipmentRepository.findAll(filter, pageable);
    }

    public List<Shipment> createShipmentFromBooking(Booking booking) {
        List<Shipment> shipments = new LinkedList<>();
        for (int i = 0; i < booking.getNumOfShipments(); i++) {
            Shipment shipment = new Shipment();
            shipment.setCreatedOn(LocalDateTime.now());
            shipment.setCreatedBy(booking.getCreatedBy());
            shipment.setBooking(booking);
            shipment.setShipper(booking.getShipper());
            shipment.setCarrier(booking.getCarrier());
            shipment.setNumOfContainers(booking.getNumOfContainers());
            shipment.setStatus(ShipmentStatus.NEW);
            shipment.setCommodity(booking.getCommodity());
            shipments.add(shipment);
        }
        return saveAllShipments(shipments);
    }

    @Transactional
    public void addContainerDetailsToShipment(Shipment shipment, List<ContainerDetails> containerList) {
        shipment.setContainerDetails(new LinkedList<>());
        saveShipment(shipment);
        containerDetailsService.deleteAll(shipment.getContainerDetails());
        List<ContainerDetails> savedContainerDetails = containerDetailsService.saveAll(containerList);
        shipment.setContainerDetails(savedContainerDetails);
        saveShipment(shipment);
    }

    public void addScheduleToShipment(Shipment shipment, Schedule schedule, Set<Transshipment> transshipmentSet) {
        if (!transshipmentSet.isEmpty()) {
            List<Transshipment> savedTransshipments = transshipmentRepository.saveAll(transshipmentSet);
            schedule.setTransshipments(savedTransshipments);
        }
        Schedule savedSchedule = scheduleService.saveSchedule(schedule);
        shipment.setSchedule(savedSchedule);
        saveShipment(shipment);
    }
}
