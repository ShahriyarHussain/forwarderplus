package com.lazoft.forwarderplus.service.finance;

import com.lazoft.forwarderplus.entity.finance.Transaction;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface TransactionService {

    void saveTransaction(Transaction transaction);

    Page<TransactionLeg> getShipmentsByFilter(Pageable pageable, Specification<TransactionLeg> filter);

}
