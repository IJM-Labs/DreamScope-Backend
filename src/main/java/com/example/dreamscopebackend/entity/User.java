package com.example.dreamscopebackend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "email_encrypted", nullable = false, columnDefinition = "TEXT")
    private String emailEncrypted;

    @Column(name = "email_hash", nullable = false, unique = true, length = 64)
    private String emailHash;

    @Column(name = "nickname_encrypted", nullable = false, columnDefinition = "TEXT")
    private String nicknameEncrypted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dream> dreams = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MagicLink> magicLinks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTerms> acceptedTerms = new ArrayList<>();

    public UUID getUserId() {
        return userId;
    }

    public String getEmailEncrypted() {
        return emailEncrypted;
    }

    public void setEmailEncrypted(String emailEncrypted) {
        this.emailEncrypted = emailEncrypted;
    }

    public String getEmailHash() {
        return emailHash;
    }

    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }

    public String getNicknameEncrypted() {
        return nicknameEncrypted;
    }

    public void setNicknameEncrypted(String nicknameEncrypted) {
        this.nicknameEncrypted = nicknameEncrypted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Dream> getDreams() {
        return dreams;
    }

    public List<MagicLink> getMagicLinks() {
        return magicLinks;
    }

    public List<UserTerms> getAcceptedTerms() {
        return acceptedTerms;
    }
}
