package com.lazoft.forwarderplus.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class CurrencyData {

    @Id
    private String code;

    private String name;
    private String country;
    private String symbol;
    private BigDecimal conversionRate;
    private LocalDateTime lastUpdated;
}
