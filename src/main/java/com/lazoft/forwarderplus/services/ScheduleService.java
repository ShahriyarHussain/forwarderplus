package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.entity.Schedule;
import com.lazoft.forwarderplus.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public List<Schedule> getScheduleByPolAndPodAndDate(Port portOfLoading, Port portOfDestination, LocalDate date) {
        return scheduleRepository.findAllByPolAndPodAndDate(portOfLoading, portOfDestination, date);
    }
}
