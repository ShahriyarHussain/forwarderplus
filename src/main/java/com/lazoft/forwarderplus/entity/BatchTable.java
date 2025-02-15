package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class BatchTable {
    @Id
    private long batchNo;
    private LocalDate businessDate;
}
