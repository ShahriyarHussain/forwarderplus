package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Reminder;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.ReminderVisibility;
import com.lazoft.forwarderplus.enums.Role;
import com.lazoft.forwarderplus.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepository;

    public List<Reminder> getRemindersByDateAndRoles(LocalDate date, User user) {
        List<Reminder> reminders = reminderRepository.getRemindersByDate(date);
        if (reminders.isEmpty()) {
            return reminders;
        }
        return reminders.stream().filter( reminder -> reminder.getVisibility() == ReminderVisibility.ALL
                || (reminder.getVisibility() == ReminderVisibility.SELF
                && reminder.getCreatedBy().getUsername().equals(user.getUsername()))
                || (reminder.getVisibility() == ReminderVisibility.SAME_ROLE && reminder.getCreatedBy().getRoles().stream()
                .filter(role -> role != Role.USER).anyMatch(role -> user.getRoles().contains(role)))).toList();
    }

    public Reminder saveData(Reminder reminder) {
        return reminderRepository.save(reminder);
    }

    public void saveDataSet(Set<Reminder> reminders) {
        reminderRepository.saveAll(reminders);
    }

    public void remove(Reminder reminder) {
        reminderRepository.delete(reminder);
    }
}
