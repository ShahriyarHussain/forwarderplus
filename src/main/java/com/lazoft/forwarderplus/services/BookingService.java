package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Booking;
import com.lazoft.forwarderplus.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    public void createBooking(Booking booking) {
        bookingRepository.save(booking);
    }

    public Optional<Booking> getBooking(String bookingNo) {
        return bookingRepository.findBookingByBookingNo(bookingNo);
    }
}
