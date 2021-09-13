package com.pradale.kterm.db.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@Entity
@SuperBuilder(toBuilder = true)
@Table(name = "server")
public class Server extends AuditEntity {

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private String username;

    private String password;

}
