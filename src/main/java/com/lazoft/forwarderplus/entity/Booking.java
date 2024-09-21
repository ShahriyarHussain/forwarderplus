package com.lazoft.forwarderplus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
//@Table(indexes = @Index(name = "booking_no_idx", columnList = "bookingNo"))
public class Booking  {
    @Id
    private String bookingNo;
    private String containerType;
    private String containerSize;
    private int numOfContainers;
    private String commodity;
    private String remarks;
    private LocalDateTime createdOn;

    @OneToOne
    private User createdBy;
    @OneToOne
    private Port loadingPort;
    @OneToOne
    private Port destinationPort;
    @OneToOne
    private Client shipper;

    @OneToMany(mappedBy = "blNo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Shipment> shipments;
}
