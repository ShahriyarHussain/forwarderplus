package com.lazoft.forwarderplus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Transshipment {

    @Transient
    private int sl;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgenerator")
    @SequenceGenerator(name = "idgenerator", initialValue = 1000)
    private long transshipmentId;

    @ManyToOne
    private Port vesselPort;
    private String vesselName;
    private LocalDate portEta;
    private String remarks;
}
