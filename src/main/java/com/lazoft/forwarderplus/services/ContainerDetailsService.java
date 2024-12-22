package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.ContainerDetails;
import com.lazoft.forwarderplus.repository.ContainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerDetailsService {

    private final ContainerRepository containerRepository;

    public List<ContainerDetails> saveAll(List<ContainerDetails> containerList) {
        return containerRepository.saveAll(containerList);
    }
}
