package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.IdGeneration;
import com.lazoft.forwarderplus.enums.IdTypes;
import com.lazoft.forwarderplus.repository.IdGenerationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IdGenerationService {

    private final IdGenerationRepository idGenerationRepository;

    public IdGeneration saveId(IdGeneration idGeneration) {
        return idGenerationRepository.save(idGeneration);
    }

    public Optional<IdGeneration> getIdByTypeAndName(IdTypes idTypes) {
        return idGenerationRepository.getIdGenerationByIdTypes(idTypes);
    }

    @Transactional
    public IdGeneration getIncrementedId(IdTypes idTypes) {
        Optional<IdGeneration> idGeneration = getIdByTypeAndName(idTypes);
        if (idGeneration.isEmpty()) {
            throw new IllegalArgumentException("Id Not Found");
        }
        IdGeneration currentIdGeneration = idGeneration.get();
        currentIdGeneration.setIncrementNum(currentIdGeneration.getIncrementNum() + currentIdGeneration.getIncrementBy());
        return idGenerationRepository.save(currentIdGeneration);
    }

    @Transactional
    public void deleteIds(Set<IdGeneration> selectedItems) {
        idGenerationRepository.deleteAll(selectedItems);
    }

    public Page<IdGeneration> getAllIds(Pageable pageable) {
        return idGenerationRepository.findAll(pageable);
    }
}
