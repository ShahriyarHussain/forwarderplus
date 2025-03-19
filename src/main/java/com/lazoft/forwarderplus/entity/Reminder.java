package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.ReminderVisibility;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Reminder extends AbstractEntity {

    private long referenceId;
    private String message;
    private LocalDate remindOn;
    private ReminderVisibility visibility;
    private boolean completed;
    @ManyToOne
    private User createdBy;
}
