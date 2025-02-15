package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Account;
import com.lazoft.forwarderplus.entity.LedgerTagInfo;
import com.lazoft.forwarderplus.repository.AccountRepository;
import com.lazoft.forwarderplus.repository.LedgerTagInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
        if (!account.getTaggedLedgers().isEmpty()) {
            List<LedgerTagInfo> savedTagInfoList = ledgerTagInfoRepository.saveAll(account.getTaggedLedgers());
            account.setTaggedLedgers(savedTagInfoList);
        }
        accountRepository.save(account);
    }

    @Transactional
    public void saveOnlyAccount(Account account) {
        accountRepository.save(account);
    }

    public Page<Account> getAccountByFilter(Specification<Account> specification, Pageable pageable) {
        return accountRepository.findAll(specification, pageable);
    }

    public Account getAccountById(long id) {
        return Objects.requireNonNull(accountRepository.findById(id).orElse(null));
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

}
