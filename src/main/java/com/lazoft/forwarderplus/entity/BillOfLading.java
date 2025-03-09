package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Entity
@Getter
@Setter
public class BillOfLading {

    @Id
    private long shipmentId;

    private String shipper;
    private String consignee;
    private String notifyParty;
    private String deliveryAgent;
    private String alsoNotifyParty;
    private String exportReference;
    private String vesselVoyage;
    private String motherVessel;
    private String portOfLoading;
    private String portOfDischarge;
    private String placeOfDelivery;
    private String placeOfReceipt;
    @Length(max = 4000)
    private String shipperMarks;
    @Length(max = 4000)
    private String goodsDescription;
    private String grossWeight;
    private String measurement;
    private String blNo;
    private String mblNo;
    private String bookingNo;
    private String container;
    private String quantity;
    private String containerNumbers;
    private String containerSeals;
    private String freightTerm;
    private String remarks;


}
