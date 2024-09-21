package com.lazoft.forwarderplus.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
//@Table(indexes = @Index(name = "shipment_blno_idx", columnList = "blNo"))
public class Shipment {

    @Id
    private String blNo;

    private String marks;
    private String goodsDescription;
    private LocalDateTime createdOn;
    private String clientInvoiceNo;

    @OneToOne
    private User createdBy;
    @OneToOne
    private Client consignee;
    @OneToOne
    private Client notifyParty;
    @OneToOne(mappedBy = "shipment", cascade = CascadeType.ALL)
    private ContainerDetails containerDetails;
    @OneToOne
    private StuffingDetails stuffingDetails;

    @ManyToOne
    @JoinColumn(name = "bookingNo", nullable = false)
    private Booking booking;
}
