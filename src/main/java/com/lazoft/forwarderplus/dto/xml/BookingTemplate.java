package com.lazoft.forwarderplus.dto.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@AllArgsConstructor
@NoArgsConstructor
public class BookingTemplate {
    @XmlElement(name = "numOfContainer")
    private Integer numOfContainer;

    @XmlElement(name = "containerType")
    private String containerType;

    @XmlElement(name = "containerSize")
    private String containerSize;

    @XmlElement(name = "commodity")
    private String commodity;

    @XmlElement(name = "carrier")
    private Integer carrier;

    @XmlElement(name = "portOfLoading")
    private Integer portOfLoading;

    @XmlElement(name = "portOfDestination")
    private Integer portOfDestination;

    @XmlElement(name = "shipper")
    private Integer shipper;
}
