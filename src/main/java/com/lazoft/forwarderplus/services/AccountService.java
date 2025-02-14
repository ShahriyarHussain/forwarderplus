package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Account;
import com.lazoft.forwarderplus.entity.LedgerTagInfo;
import com.lazoft.forwarderplus.repository.AccountRepository;
import com.lazoft.forwarderplus.repository.LedgerTagInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerTagInfoRepository ledgerTagInfoRepository;

    public void save(Account account) {
        accountRepository.save(account);
    }

    public boolean accountAlreadyExists(String accountNo) {
        return accountRepository.accountExistsByAccountNumber(accountNo);
    }

    @Transactional
    public void saveAccount(Account account) {
        if (account.getTaggedLedgers() != null && !account.getTaggedLedgers().isEmpty()) {
            List<LedgerTagInfo> savedTagInfoList = ledgerTagInfoRepository.saveAll(account.getTaggedLedgers());
            account.setTaggedLedgers(savedTagInfoList);
        }
        accountRepository.save(account);
    }


}
