package com.lazoft.forwarderplus.repository.finance;

import com.lazoft.forwarderplus.entity.finance.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
