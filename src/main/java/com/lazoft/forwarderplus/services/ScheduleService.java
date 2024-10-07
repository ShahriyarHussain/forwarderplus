package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Schedule;
import com.lazoft.forwarderplus.entity.Shipment;
import com.lazoft.forwarderplus.repository.ScheduleRepository;
import com.lazoft.forwarderplus.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}
