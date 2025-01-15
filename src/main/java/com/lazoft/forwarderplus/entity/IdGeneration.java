package com.lazoft.forwarderplus.entity;

import com.lazoft.forwarderplus.enums.IdTypes;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class IdGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String suffix;
    private Long incrementNum;
    private String prefix;
    private int incrementBy;
    private IdTypes alwaysUseFor;
}
