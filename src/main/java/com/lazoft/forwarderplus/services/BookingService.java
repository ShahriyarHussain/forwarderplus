package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShipmentService shipmentService;


    @Transactional
    public void createBooking(Booking booking) {
        Shipment shipment = createShipmentFromBooking(booking);
        bookingRepository.save(booking);
        shipmentService.saveShipment(shipment);
    }

    public Optional<Booking> getBooking(String bookingNo) {
        return bookingRepository.findBookingByBookingNo(bookingNo);
    }

    private Shipment createShipmentFromBooking(Booking booking) {
        Shipment shipment = new Shipment();
        shipment.setBlNo(booking.getBookingNo());
        shipment.setCreatedOn(LocalDateTime.now());
        shipment.setCreatedBy(booking.getCreatedBy());
        shipment.setBooking(booking);
        return shipment;
    }
}
