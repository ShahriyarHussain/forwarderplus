package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.repository.PortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortService {

    private final PortRepository portRepository;

    public List<Port> getAllPorts() {
        return portRepository.findAllByOrderByPortShortCodeAsc();
    }
}
