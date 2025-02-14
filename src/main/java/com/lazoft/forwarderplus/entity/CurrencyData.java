package com.lazoft.forwarderplus.entity;


import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyData extends AbstractEntity {

    private String baseCurrencyCode;
    private String targetCurrencyCode;
    private BigDecimal conversionRate;
    private LocalDateTime lastUpdated;
    private boolean autoUpdateDisabled;
}
