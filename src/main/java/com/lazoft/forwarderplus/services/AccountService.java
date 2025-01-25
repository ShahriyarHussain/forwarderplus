package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.Account;
import com.lazoft.forwarderplus.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public void save(Account account) {
        accountRepository.save(account);
    }

    public boolean accountAlreadyExists(String accountNo) {
        return accountRepository.accountExistsByAccountNumber(accountNo);
    }


}
