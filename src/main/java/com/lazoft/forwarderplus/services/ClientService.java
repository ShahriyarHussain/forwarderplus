package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Client;
import com.lazoft.forwarderplus.enums.ClientType;
import com.lazoft.forwarderplus.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

    public Page<Client> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    @Transactional
    public void deleteClients(Set<Client> clients) {
        clientRepository.deleteAll(clients);
    }
}
