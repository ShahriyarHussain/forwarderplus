package com.lazoft.forwarderplus.repository.finance;

import com.lazoft.forwarderplus.dto.AccountTypeSummaryDto;
import com.lazoft.forwarderplus.entity.finance.TransactionLeg;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface TransactionLegQueryRepository {
    List<AccountTypeSummaryDto> getSummariesByAccountType(Specification<TransactionLeg> spec);
}