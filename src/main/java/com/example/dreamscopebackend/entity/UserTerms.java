package com.example.dreamscopebackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users_terms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "terms_id"})
)
public class UserTerms {
    @Id
    @GeneratedValue
    @Column(name = "user_terms_id", nullable = false, updatable = false)
    private UUID userTermsId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terms_id", nullable = false)
    private TermsAndConditions terms;

    @Column(name = "accepted_at", nullable = false, updatable = false)
    private Instant acceptedAt = Instant.now();

    public UUID getUserTermsId() {
        return userTermsId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TermsAndConditions getTerms() {
        return terms;
    }

    public void setTerms(TermsAndConditions terms) {
        this.terms = terms;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }
}
