package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.repository.ClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;


    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll(Sort.by(Sort.Order.asc("name")));
    }

    public List<Client> getClientsByType(List<ClientType> types) {
        return clientRepository.findClientsByType(types);
    }
}
