package com.lazoft.forwarderplus.entity;


import com.lazoft.forwarderplus.enums.ShipmentStatus;
import com.lazoft.forwarderplus.enums.ShippingTerm;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    private String goodsDescription;
    private String shipperMarks;
    private String clientInvoiceNo;
    private String commodity;
    private int numOfContainers;

    private ShippingTerm shippingTerm;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "id")
    private Carrier carrier;
//    @ManyToOne
//    private Commodity commodity;
    @ManyToOne
    private User createdBy;
    @ManyToOne
    private Client shipper;
    @ManyToOne
    private Client consignee;
    @ManyToOne
    private Client notifyParty;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContainerDetails> containerDetails;
    @OneToOne(fetch = FetchType.EAGER)
    private StuffingDetails stuffingDetails;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scheduleId")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bookingNo", nullable = false)
    private Booking booking;


}
