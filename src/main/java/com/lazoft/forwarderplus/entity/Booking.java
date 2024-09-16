package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Booking extends AbstractEntity {
    private String bookingNo;
    private String containerType;
    private String containerSize;
    private int numOfContainers;
    private String commodity;
    @OneToOne
    private Client shipper;
}
