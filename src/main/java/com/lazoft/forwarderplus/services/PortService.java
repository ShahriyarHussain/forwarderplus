package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Port;
import com.lazoft.forwarderplus.repository.PortRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PortService {

    private final PortRepository portRepository;

    public List<Port> getAllPorts() {
        return portRepository.findAllByOrderByPortShortCodeAsc();
    }

    public Page<Port> getAllPorts(Pageable pageable) {
        return portRepository.findAll(pageable);
    }

    @Transactional
    public void deletePorts(Set<Port> ports) {
        portRepository.deleteAll(ports);
    }

    public void savePort(Port port) {
        portRepository.save(port);
    }
}
