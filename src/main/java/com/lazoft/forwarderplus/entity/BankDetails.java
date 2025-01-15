package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankDetails extends AbstractEntity {
    private String bankName;
    private String accNo;
    private String accName;
    private String routingNo;
    private String branchName;
}