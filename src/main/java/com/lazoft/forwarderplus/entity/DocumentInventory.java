package com.lazoft.forwarderplus.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DocumentInventory extends AbstractEntity {
    private String name;
    private String description;
    private String sourceId;
    private String resourceLocation;
}
