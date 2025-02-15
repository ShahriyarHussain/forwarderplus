package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Ledger;
import com.lazoft.forwarderplus.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;


    public void saveLedger(Ledger ledger) {
        ledgerRepository.save(ledger);
    }

    public boolean ledgerAlreadyExistsByCode(String code) {
        return ledgerRepository.findLedgerByCode(code).isPresent();
    }

    public Optional<Ledger> getLedgerByCode(String code) {
        return ledgerRepository.findLedgerByCode(code);
    }

    public List<Ledger> getAllLedgers() {
        return ledgerRepository.findAll();
    }

    public Page<Ledger> getLedgersByFilter(Specification<Ledger> specification, Pageable pageable) {
        return ledgerRepository.findAll(specification, pageable);
    }

    public List<Ledger> getLedgers() {
        return ledgerRepository.findAll();
    }
}
