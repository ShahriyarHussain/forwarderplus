package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.StuffingDetails;
import com.lazoft.forwarderplus.repository.StuffingDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StuffingDetailsService {
    private final StuffingDetailsRepository stuffingDetailsRepository;

    public StuffingDetails saveStuffingDetails(StuffingDetails stuffingDetails) {
        return stuffingDetailsRepository.save(stuffingDetails);
    }

}
