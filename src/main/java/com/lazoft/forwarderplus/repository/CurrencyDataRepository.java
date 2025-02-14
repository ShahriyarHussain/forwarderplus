package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.CurrencyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyDataRepository extends JpaRepository<CurrencyData, String> {

    @Query("select c from CurrencyData c where c.baseCurrencyCode = :baseCurrCode and c.targetCurrencyCode = :targetCurrCode")
    Optional<CurrencyData> getConversionRateByCurrency(@Param("baseCurrCode") String baseCurrency,
                                                       @Param("targetCurrCode") String targetCurrency);
}
