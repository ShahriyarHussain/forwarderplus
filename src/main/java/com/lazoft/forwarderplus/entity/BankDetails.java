package com.lazoft.forwarderplus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BankDetails extends AbstractEntity {
    private String bankName;
    private String accNo;
    private String accName;
    private String routingNo;
    private String branchName;
}