package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.IdGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdGenerationRepository extends JpaRepository<IdGeneration, Long> {
}
