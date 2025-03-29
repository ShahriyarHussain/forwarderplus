package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {

    @Query("select s from Schedule s where s.portOfLoading = :pol and s.portOfDestination = :pod and " +
            "(s.portOfDestinationETA is null or s.portOfLoadingETA > :date)")
    List<Schedule> findAllByPolAndPodAndDate(@Param("pol") Port pol, @Param("pod") Port pod,
                                             @Param("date") LocalDate date);
}
