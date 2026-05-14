package com.example.dreamscopebackend.repository;

import com.example.dreamscopebackend.entity.Dream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DreamRepository extends JpaRepository<Dream, UUID> {
    List<Dream> findByUserUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Dream> findByDreamIdAndUserUserId(UUID dreamId, UUID userId);

    List<Dream> findByUserUserIdAndThreadIdOrderByCreatedAtDesc(UUID userId, String threadId);

    void deleteByUserUserId(UUID userId);
}
