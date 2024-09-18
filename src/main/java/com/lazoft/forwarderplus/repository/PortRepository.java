package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Port;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PortRepository extends JpaRepository<Port, Long>, JpaSpecificationExecutor<Port> {

    List<Port> findAllByOrderByPortShortCodeAsc();

}
