package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.IdGeneration;
import com.lazoft.forwarderplus.enums.IdTypes;
import com.lazoft.forwarderplus.repository.IdGenerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdGenerationService {

    private final IdGenerationRepository idGenerationRepository;

    public IdGeneration saveId(IdGeneration idGeneration) {
        return idGenerationRepository.save(idGeneration);
    }

    public Optional<IdGeneration> getIdByTypeAndName(String name, IdTypes idTypes) {
        return idGenerationRepository.getIdGenerationByNameAndIdTypes(name, idTypes);
    }

    public IdGeneration getIncrementedId(String name, IdTypes idTypes) {
        Optional<IdGeneration> idGeneration = getIdByTypeAndName(name, idTypes);
        if (idGeneration.isEmpty()) {
            IdGeneration newIdGeneration = new IdGeneration();
            newIdGeneration.setName(name);
            newIdGeneration.setIncrementBy(1);
            newIdGeneration.setPrefix("");
            newIdGeneration.setSuffix("");
            newIdGeneration.setAlwaysUseFor(idTypes);
            newIdGeneration.setIncrementNum(1L);
            return idGenerationRepository.save(newIdGeneration);
        }
        IdGeneration currentIdGeneration = idGeneration.get();
        currentIdGeneration.setIncrementNum(currentIdGeneration.getIncrementNum() + currentIdGeneration.getIncrementBy());
        return idGenerationRepository.save(currentIdGeneration);
    }
}
