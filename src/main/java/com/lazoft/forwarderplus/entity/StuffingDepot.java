package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class StuffingDepot extends AbstractEntity {
    private String depotName;
    private String depotLocation;
    private String depotCode;
}
