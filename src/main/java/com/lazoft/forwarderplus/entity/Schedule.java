package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.util.DateUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_id_generator")
    @SequenceGenerator(name = "schedule_id_generator")
    private Long scheduleId;

    @ManyToOne
    private Port portOfLoading;
    private LocalDate portOfLoadingETA;
    private LocalDate portOfLoadingETD;

    @ManyToOne
    private Port portOfDestination;
    private LocalDate portOfDestinationETA;

    @ManyToOne
    private Port motherVesselPort;
    private LocalDate motherVesselETA;

    private String portOfLoadingVesselName;
    private String motherVesselName;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private Set<Shipment> shipment;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.EAGER)
    private List<Transshipment> transshipments;

    public String getScheduleSummary() {
        return portOfLoading.getPortName() + " To " + portOfDestination.getPortName() + ", Departure: " +
                DateUtil.getDateAsString(portOfLoadingETD) + ", Transit: " +
                ChronoUnit.DAYS.between(portOfLoadingETD, portOfDestinationETA) + " day(s)";
    }

}
