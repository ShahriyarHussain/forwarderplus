package com.lazoft.forwarderplus.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
public class Booking {

    @Id
    private String bookingNo;
    private String containerType;
    private String containerSize;
    private int numOfContainers;
    private String commodity;
    private String remarks;
    private LocalDateTime createdOn;

    @ManyToOne
    private User createdBy;
    @ManyToOne
    private Port loadingPort;
    @ManyToOne
    private Port destinationPort;
    @ManyToOne
    private Client shipper;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<Shipment> shipments;

    @Transient
    private int numOfShipments;
    @Transient
    private Carrier carrier;
}
