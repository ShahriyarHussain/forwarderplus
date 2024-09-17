package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Carrier;
import com.lazoft.forwarderplus.repository.CarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarrierService {
    private final CarrierRepository carrierRepository;

    public List<Carrier> getAllCarriers() {
        return carrierRepository.findAllByOrderByNameAsc();
    }
}
