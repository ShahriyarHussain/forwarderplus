package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Carrier;
import com.lazoft.forwarderplus.repository.CarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CarrierService {
    private final CarrierRepository carrierRepository;

    public List<Carrier> getAllCarriers() {
        return carrierRepository.findAllByOrderByNameAsc();
    }

    public Page<Carrier> getAllCarriers(Pageable pageable) {
        return carrierRepository.findAll(pageable);
    }

    @Transactional
    public void deleteCarriers(Set<Carrier> carriers) {
        carrierRepository.deleteAll(carriers);
    }

    public void savePort(Carrier carrier) {
        carrierRepository.save(carrier);
    }
}
