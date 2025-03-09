package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.BillOfLading;
import com.lazoft.forwarderplus.repository.BillOfLadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BillOfLadingService {

    private final BillOfLadingRepository billOfLadingRepository;

    @Transactional
    public void saveBillOfLading(BillOfLading billOfLading) {
        billOfLadingRepository.save(billOfLading);
    }

    @Transactional
    public void deleteBillOfLading(BillOfLading billOfLading) {
        if (getBillOfLadingByShipmentId(billOfLading.getShipmentId()).isPresent()) {
            billOfLadingRepository.delete(billOfLading);
        }
    }

    public Optional<BillOfLading> getBillOfLadingByShipmentId(long shipmentId) {
        return billOfLadingRepository.findById(shipmentId);
    }
}
