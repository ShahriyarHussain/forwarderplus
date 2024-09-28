package com.lazoft.forwarderplus.entity;


import com.lazoft.forwarderplus.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.mapping.ToOne;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
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
    private String clientInvoiceNo;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;
    private LocalDateTime createdOn;

    @ManyToOne
    private User createdBy;
    @ManyToOne
    private Client shipper;
    @ManyToOne
    private Client consignee;
    @ManyToOne
    private Client notifyParty;
    @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY)
    private ContainerDetails containerDetails;
    @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY)
    private StuffingDetails stuffingDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleId")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "bookingNo", nullable = false)
    private Booking booking;


}
