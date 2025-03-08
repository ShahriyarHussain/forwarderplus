package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.BankDetails;
import com.lazoft.forwarderplus.repository.BankDetailsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BankDetailsService {

    private final BankDetailsRepository bankDetailsRepository;

    public List<BankDetails> getBankDetails() {
        return bankDetailsRepository.findAll();
    }


    public Page<BankDetails> getAllBankDetails(Pageable pageable) {
        return bankDetailsRepository.findAll(pageable);
    }

    @Transactional
    public void deleteBankDetails(Set<BankDetails> bankDetailsSet) {
        bankDetailsRepository.deleteAll(bankDetailsSet);
    }

    public void saveBankDetails(BankDetails bankDetails) {
        bankDetailsRepository.save(bankDetails);
    }
}
