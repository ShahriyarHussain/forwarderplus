package com.lazoft.forwarderplus.services.finance.impl;

import com.lazoft.forwarderplus.entity.BatchInfo;
import com.lazoft.forwarderplus.repository.BatchInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BatchNoService {

    private final BatchInfoRepository batchInfoRepository;

    public int getBatchNoByDate(LocalDate date) {
        Optional<BatchInfo> existingBatchInfo = batchInfoRepository.findByBusinessDate(date);
        if (existingBatchInfo.isPresent()) {
            BatchInfo batchInfo = existingBatchInfo.get();
            batchInfo.setBatchNo(batchInfo.getBatchNo() + 1);
            return batchInfoRepository.save(batchInfo).getBatchNo();
        } else {
            BatchInfo newBatchInfo = new BatchInfo();
            newBatchInfo.setBusinessDate(date);
            newBatchInfo.setBatchNo(1);
            return batchInfoRepository.save(newBatchInfo).getBatchNo();
        }
    }
}
