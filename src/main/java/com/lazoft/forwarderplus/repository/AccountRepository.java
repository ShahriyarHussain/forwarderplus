package com.lazoft.forwarderplus.repository;

import com.lazoft.forwarderplus.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    @Query("select count(a) > 0 from Account a where a.accountNo = :accountNo")
    boolean accountExistsByAccountNumber(@Param("accountNo") String accountNo);
}
