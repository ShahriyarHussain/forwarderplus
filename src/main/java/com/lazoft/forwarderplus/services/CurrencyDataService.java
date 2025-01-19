package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.dto.ExchangeRateResponseDTO;
import com.lazoft.forwarderplus.entity.CurrencyData;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.repository.CurrencyDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CurrencyDataService {

    private final CurrencyDataRepository currencyDataRepository;

    private final String SEPARATOR = "/";

    @Value("${exchange.api.link}")
    private String apiUrl;

    private CurrencyData getCurrencyDataFromAPI(String base, String target) {
        RestClient restClient = RestClient.create();
        ExchangeRateResponseDTO responseDTO = restClient.get()
                .uri(apiUrl + SEPARATOR + base + SEPARATOR + target)
                .retrieve().body(ExchangeRateResponseDTO.class);
        if (responseDTO == null || !responseDTO.getResult().equalsIgnoreCase("success")) { // remove magic string
            throw new InvalidDataAccessApiUsageException("Could fetch data from API");
        }
        return new CurrencyData(base, target, BigDecimal.valueOf(responseDTO.getConversionRate()), LocalDateTime.now());
    }

    public BigDecimal getConversionRateByCurrency(AmountCurrency base, AmountCurrency target) {
        Optional<CurrencyData> currencyData = currencyDataRepository
                .getConversionRateByCurrency(base.toString(), target.toString());

        if (currencyData.isEmpty()) { //fix adding same data multiple times
            CurrencyData newCurrencyData = getCurrencyDataFromAPI(base.toString(), target.toString());
            currencyDataRepository.save(newCurrencyData);
            return newCurrencyData.getConversionRate();
        } else if (ChronoUnit.DAYS.between(currencyData.get().getLastUpdated(), LocalDateTime.now()) < 2) {
            CurrencyData newCurrencyData = getCurrencyDataFromAPI(base.toString(), target.toString());
            CurrencyData oldDataForUpdate = currencyData.get();
            oldDataForUpdate.setConversionRate(newCurrencyData.getConversionRate());
            currencyDataRepository.save(oldDataForUpdate);
            return oldDataForUpdate.getConversionRate();
        }
        return currencyData.get().getConversionRate();
    }
}
