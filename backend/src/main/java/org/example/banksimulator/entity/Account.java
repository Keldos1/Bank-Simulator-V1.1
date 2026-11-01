package org.example.banksimulator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.banksimulator.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users_account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Column(nullable = false)
    private Instant openedAt;
    private Instant closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id",nullable = false)
    private User owner;

    @OneToMany(mappedBy = "sourceAccount")
    private List<Transaction> sourceTransaction;

    @OneToMany(mappedBy = "destinationAccount")
    private List<Transaction> destinationTransaction;

    @PrePersist
    private void prePersist(){
        this.openedAt = Instant.now();
    }
}
