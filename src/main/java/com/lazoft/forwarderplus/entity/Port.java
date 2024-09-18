package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Entity
@Getter
@Setter
public class Port extends AbstractEntity {

    private String portShortCode;
    @Length(max = 100)
    private String portName;
    @Length(max = 100)
    private String portCountry;
    private String portCity;

    public String getPortLabel() {
        return getPortShortCode() + " - " + getPortName() + ", " + getPortCountry();
    }

    public String getPortCityAndCountry() {
        return getPortName() + ", " + getPortCountry();
    }
}
