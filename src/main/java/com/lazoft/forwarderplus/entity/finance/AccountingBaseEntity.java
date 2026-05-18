package com.lazoft.forwarderplus.entity.finance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class AccountingBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fin_id_generator")
    @SequenceGenerator(name = "fin_id_generator", initialValue = 100, allocationSize = 1)
    private Long id;

    @Column(name = "uuid", unique = true, updatable = false, nullable = false)
    private String uuid = UUID.randomUUID().toString();

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AccountingBaseEntity that)) return false;
        return new EqualsBuilder().append(getUuid(), that.getUuid()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getUuid()).toHashCode();
    }
}