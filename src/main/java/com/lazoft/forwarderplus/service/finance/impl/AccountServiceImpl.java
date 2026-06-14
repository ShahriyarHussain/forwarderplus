package com.lazoft.forwarderplus.service.finance.impl;

import com.lazoft.forwarderplus.entity.finance.Account;
import com.lazoft.forwarderplus.repository.finance.AccountRepository;
import com.lazoft.forwarderplus.service.finance.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public void addAccount(Account account) {
        accountRepository.save(account);
    }

    @Override
    public void removeAccount(Account account) {
        accountRepository.delete(account);
    }
}
