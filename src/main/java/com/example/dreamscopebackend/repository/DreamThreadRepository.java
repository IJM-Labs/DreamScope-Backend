package com.example.dreamscopebackend.repository;

import com.example.dreamscopebackend.entity.DreamThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DreamThreadRepository extends JpaRepository<DreamThread, UUID> {
    Optional<DreamThread> findByThreadIdAndUserUserId(String threadId, UUID userId);

    List<DreamThread> findByUserUserId(UUID userId);

    void deleteByUserUserId(UUID userId);
}
