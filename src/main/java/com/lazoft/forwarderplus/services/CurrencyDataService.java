package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.dto.ExchangeRateResponseDTO;
import com.lazoft.forwarderplus.entity.CurrencyData;
import com.lazoft.forwarderplus.enums.AmountCurrency;
import com.lazoft.forwarderplus.repository.CurrencyDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyDataService {

    private final CurrencyDataRepository currencyDataRepository;

    @Value("${exchange.api.url}")
    private String apiUrl;

    @Value("${exchange.api.version}")
    private String apiVersion;

    @Value("${exchange.api.pair.conversion.endpoint}")
    private String pairConversionEndpoint;

    @Value("${exchange.api.key}")
    private String apiKey;

    private CurrencyData getCurrencyDataFromAPI(String base, String target) {
        RestClient restClient = RestClient.create();
        ExchangeRateResponseDTO responseDTO;
        try {
            responseDTO = restClient.get().uri(apiUrl + apiVersion + File.separator + apiKey +
                            pairConversionEndpoint + File.separator + base + File.separator + target)
                    .retrieve().body(ExchangeRateResponseDTO.class);
        } catch (Exception e) {
            log.error("Currency data API error", e);
            throw e;
        }

        String API_SUCCESS_STRING = "success";
        if (responseDTO == null || !responseDTO.getResult().equalsIgnoreCase(API_SUCCESS_STRING)) {
            throw new InvalidDataAccessApiUsageException("Could Not fetch data from API");
        }
        return new CurrencyData(base, target, BigDecimal.valueOf(responseDTO.getConversionRate()), LocalDateTime.now(), false);
    }

    public BigDecimal getConversionRateByCurrency(AmountCurrency base, AmountCurrency target) {
        Optional<CurrencyData> currencyData = currencyDataRepository.getConversionRateByCurrency(base.toString(), target.toString());
        if (currencyData.isEmpty()) {
            CurrencyData newCurrencyData = getCurrencyDataFromAPI(base.toString(), target.toString());
            currencyDataRepository.save(newCurrencyData);
            return newCurrencyData.getConversionRate();
        }

        CurrencyData oldDataForUpdate = currencyData.get();
        BigDecimal conversionRate = oldDataForUpdate.getConversionRate();
        if (oldDataForUpdate.isAutoUpdateDisabled()) {
            return conversionRate;
        }
        if (ChronoUnit.DAYS.between(oldDataForUpdate.getLastUpdated(), LocalDateTime.now()) > 2) {
            try {
                CurrencyData newCurrencyData = getCurrencyDataFromAPI(base.toString(), target.toString());
                oldDataForUpdate.setConversionRate(newCurrencyData.getConversionRate());
                oldDataForUpdate.setLastUpdated(LocalDateTime.now());
                oldDataForUpdate = currencyDataRepository.save(oldDataForUpdate);
                conversionRate = oldDataForUpdate.getConversionRate();
            } catch (Exception e) {
                log.error("Currency data API error: {}", e.getMessage());
            }
        }
        return conversionRate;
    }
}
