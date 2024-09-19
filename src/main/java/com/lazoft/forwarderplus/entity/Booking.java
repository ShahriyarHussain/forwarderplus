package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "booking_no_idx", columnList = "bookingNo"))
public class Booking extends AbstractEntity {
    private String bookingNo;
    private String containerType;
    private String containerSize;
    private int numOfContainers;
    private String commodity;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdOn;
    private String modifiedBy;
    private LocalDateTime modifiedOn;
    @OneToOne
    private Port loadingPort;
    @OneToOne
    private Port destinationPort;
    @OneToOne
    private Client shipper;
}
