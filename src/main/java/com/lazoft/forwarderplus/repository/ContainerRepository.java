package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.ContainerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContainerRepository extends JpaRepository<ContainerDetails, String> {


}
