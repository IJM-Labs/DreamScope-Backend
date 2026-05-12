package com.example.dreamscopebackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interpretations")
public class Interpretation {
    @Id
    @GeneratedValue
    @Column(name = "interpretation_id", nullable = false, updatable = false)
    private UUID interpretationId;

    @Column(name = "text_encrypted", nullable = false, columnDefinition = "TEXT")
    private String textEncrypted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dream_id", nullable = false)
    private Dream dream;

    public UUID getInterpretationId() {
        return interpretationId;
    }

    public String getTextEncrypted() {
        return textEncrypted;
    }

    public void setTextEncrypted(String textEncrypted) {
        this.textEncrypted = textEncrypted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Dream getDream() {
        return dream;
    }

    public void setDream(Dream dream) {
        this.dream = dream;
    }
}
