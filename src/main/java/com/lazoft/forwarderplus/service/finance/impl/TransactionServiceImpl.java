package com.lazoft.forwarderplus.service.finance.impl;

import com.lazoft.forwarderplus.dto.AccountTypeSummaryDto;
import com.lazoft.forwarderplus.entity.finance.Transaction;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import com.lazoft.forwarderplus.repository.finance.TransactionLegRepository;
import com.lazoft.forwarderplus.repository.finance.TransactionRepository;
import com.lazoft.forwarderplus.service.finance.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionLegRepository transactionLegRepository;

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Override
    public Page<TransactionLeg> getShipmentsByFilter(Pageable pageable,
                                                     Specification<TransactionLeg> filter) {
        return transactionLegRepository.findAll(filter, pageable);
    }

    @Override
    public List<AccountTypeSummaryDto> getSummaryHeaderData(Specification<TransactionLeg> spec) {
        return transactionLegRepository.getSummariesByAccountType(spec);
    }

}
