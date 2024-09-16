package com.lazoft.forwarderplus.entity;


import com.lazoft.forwarderplus.enums.ClientType;
import com.vaadin.flow.component.tabs.Tab;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@Getter
@Setter
@Table(name = "clients")
public class Client extends AbstractEntity {
    private String name;
    private String address;

    @Email
    private String email;

    private String postCode;
    private String taxId;
    private String city;
    private String country;
    private String accountNumber;
    private String accountBank;

    @Enumerated(EnumType.STRING)
//    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private ClientType type;
}
