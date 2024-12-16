package com.lazoft.forwarderplus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Commodity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String hscode;
    private String name;
    private String description;
    private boolean isDangerousGoods;

    public String getCommoditySummary() {
        StringBuilder summary = new StringBuilder();
        if (hscode != null && !hscode.isEmpty()) {
            summary.append(hscode).append(" - ");
        }
        summary.append(name);
        if (isDangerousGoods) {
            summary.append(" (Hazardous)");
        }
        return summary.toString();
    }
}