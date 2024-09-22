package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String>, JpaSpecificationExecutor<Booking> {

    boolean existsBookingByBookingNo(String bookingNumber);

}
