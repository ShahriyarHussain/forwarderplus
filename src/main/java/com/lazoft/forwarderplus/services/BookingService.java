package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShipmentService shipmentService;


    @Transactional
    public void createBooking(Booking booking) {
        Booking savedBooking = bookingRepository.save(booking);
        Set<Shipment> shipments = shipmentService.createShipmentFromBooking(savedBooking, booking.getNumOfShipments());
        savedBooking.setShipments(shipments);
        bookingRepository.save(savedBooking);
    }

    public boolean getBooking(String bookingNo) {
        return bookingRepository.existsBookingByBookingNo(bookingNo);
    }


}
