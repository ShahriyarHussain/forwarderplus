package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.ContainerSize;
import com.lazoft.forwarderplus.enums.ContainerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Booking {

    @Id
    private String bookingNo;
    private ContainerType containerType;
    private ContainerSize containerSize;
    private int numOfContainers;
    private String remarks;
    private LocalDateTime createdOn;

    @ManyToOne
    private Carrier carrier;
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
    private List<Shipment> shipments;

    @Transient
    private int numOfShipments;
    @Transient
    private String commodity;
}
