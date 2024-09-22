package com.lazoft.forwarderplus.entity;


import com.lazoft.forwarderplus.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(indexes = {@Index(name = "shipment_mblno_idx", columnList = "mblNo"),
        @Index(name = "shipment_hblno_idx", columnList = "hblNo")})
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipment_id_generator")
    @SequenceGenerator(name = "shipment_id_generator")
    private Long shipmentId;

    private String mblNo;
    private String hblNo;

    private String marks;
    private String goodsDescription;
    private LocalDateTime createdOn;
    private String clientInvoiceNo;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @OneToOne
    private Client shipper;
    @OneToOne
    private Client consignee;
    @OneToOne
    private Client notifyParty;
    @OneToOne(fetch = FetchType.LAZY)
    private ContainerDetails containerDetails;
    @OneToOne(fetch = FetchType.LAZY)
    private StuffingDetails stuffingDetails;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleId")
    private Schedule schedule;
    @OneToOne
    @JoinColumn(name = "bookingNo", nullable = false)
    private Booking booking;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private User createdBy;
}
