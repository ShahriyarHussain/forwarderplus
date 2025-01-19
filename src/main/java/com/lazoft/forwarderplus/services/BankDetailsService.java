package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.BankDetails;
import com.lazoft.forwarderplus.repository.BankDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankDetailsService {

    private final BankDetailsRepository bankDetailsRepository;

    public List<BankDetails> getBankDetails() {
        return bankDetailsRepository.findAll();
    }


}
