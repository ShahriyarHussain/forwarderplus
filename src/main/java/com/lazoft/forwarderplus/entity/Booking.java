package com.lazoft.forwarderplus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "booking_no_idx", columnList = "bookingNo"))
public class Booking {

    @Id
    private String bookingNo;
    private String containerType;
    private String containerSize;
    private int numOfContainers;
    private String commodity;
    private String remarks;
    private LocalDateTime createdOn;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private User createdBy;
    @OneToOne
    private Port loadingPort;
    @OneToOne
    private Port destinationPort;
    @OneToOne
    private Client shipper;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Shipment> shipments;

    @Transient
    private int numOfShipments;
}
