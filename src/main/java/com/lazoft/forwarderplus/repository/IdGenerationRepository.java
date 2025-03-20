package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.IdGeneration;
import com.lazoft.forwarderplus.enums.IdTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdGenerationRepository extends JpaRepository<IdGeneration, Long> {

    @Query("select i from IdGeneration i where i.alwaysUseFor = :type")
    Optional<IdGeneration> getIdGenerationByIdTypes(@Param("type") IdTypes idTypes);
}
