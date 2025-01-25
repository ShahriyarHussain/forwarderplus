package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Ledger;
import com.lazoft.forwarderplus.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
