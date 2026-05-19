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
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "dream_threads",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "thread_id"})
)
public class DreamThread {
    @Id
    @GeneratedValue
    @Column(name = "dream_thread_id", nullable = false, updatable = false)
    private UUID dreamThreadId;

    @Column(name = "thread_id", nullable = false, length = 80)
    private String threadId;

    @Column(name = "title_encrypted")
    private String titleEncrypted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dream> dreams = new ArrayList<>();

    public UUID getDreamThreadId() {
        return dreamThreadId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getTitleEncrypted() {
        return titleEncrypted;
    }

    public void setTitleEncrypted(String titleEncrypted) {
        this.titleEncrypted = titleEncrypted;
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

    public List<Dream> getDreams() {
        return dreams;
    }
}
