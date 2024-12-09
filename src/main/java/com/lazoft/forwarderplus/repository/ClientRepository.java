package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.enums.ClientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    //List<Client> findAll();

    @Query("select c from Client c where c.type in :types")
    List<Client> findClientsByType(@Param("types") List<ClientType> types);
}
