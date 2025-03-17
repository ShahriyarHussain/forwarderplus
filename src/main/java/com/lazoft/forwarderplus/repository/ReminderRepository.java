package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {

    @Query("select r from Reminder r where r.remindOn >= :date or (r.remindOn < :date and r.completed = false)")
    List<Reminder> getRemindersByDate(@Param("date") LocalDate date);
}
