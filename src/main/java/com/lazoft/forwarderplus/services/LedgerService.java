package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Ledger;
import com.lazoft.forwarderplus.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;


    public void saveLedger(Ledger ledger) {
        ledgerRepository.save(ledger);
    }

    public boolean ledgerAlreadyExistsByCode(String code) {
        return ledgerRepository.countLedgerByCode(code) > 0;
    }
}
