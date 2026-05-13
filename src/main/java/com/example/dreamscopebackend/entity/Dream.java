package com.example.dreamscopebackend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dreams")
public class Dream {
    @Id
    @GeneratedValue
    @Column(name = "dream_id", nullable = false, updatable = false)
    private UUID dreamId;

    @Column(name = "content_encrypted", nullable = false, columnDefinition = "TEXT")
    private String contentEncrypted;

    @Column(name = "title_encrypted")
    private String titleEncrypted;

    @Column(name = "thread_id", length = 80)
    private String threadId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "dream", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interpretation> interpretations = new ArrayList<>();

    public UUID getDreamId() {
        return dreamId;
    }

    public String getContentEncrypted() {
        return contentEncrypted;
    }

    public void setContentEncrypted(String contentEncrypted) {
        this.contentEncrypted = contentEncrypted;
    }

    public String getTitleEncrypted() {
        return titleEncrypted;
    }

    public void setTitleEncrypted(String titleEncrypted) {
        this.titleEncrypted = titleEncrypted;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Interpretation> getInterpretations() {
        return interpretations;
    }
}
