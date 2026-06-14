package com.lazoft.forwarderplus.entity.finance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Metabase_Report")
public class MetabaseReport {
    @Id
    private long reportId;
    private String reportName;
    private String createdBy;
    private LocalDateTime createdOn;
}
