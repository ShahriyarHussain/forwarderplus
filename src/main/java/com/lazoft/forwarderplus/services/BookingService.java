package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShipmentService shipmentService;


    @Transactional
    public Booking createBooking(Booking booking) {
        Booking savedBooking = bookingRepository.save(booking);
        savedBooking.setCarrier(booking.getCarrier());
        savedBooking.setNumOfShipments(booking.getNumOfShipments());
        savedBooking.setCommodity(booking.getCommodity());

        List<Shipment> shipments = shipmentService.createShipmentFromBooking(savedBooking);
        savedBooking.setShipments(shipments);
        return bookingRepository.save(savedBooking);
    }

    public boolean bookingExists(String bookingNo) {
        return bookingRepository.existsBookingByBookingNo(bookingNo);
    }


}
