package com.lazoft.forwarderplus.service.finance;

import com.lazoft.forwarderplus.entity.finance.Account;

import java.util.List;

public interface AccountService {
    List<Account> getAllAccounts();

    void addAccount(Account account);

    void removeAccount(Account account);
}
