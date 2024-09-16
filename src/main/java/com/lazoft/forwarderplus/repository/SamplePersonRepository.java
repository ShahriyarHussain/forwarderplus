package com.lazoft.forwarderplus.repository;


import com.lazoft.forwarderplus.entity.SamplePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SamplePersonRepository extends
        JpaRepository<SamplePerson, Long>,
        JpaSpecificationExecutor<SamplePerson> {

}
