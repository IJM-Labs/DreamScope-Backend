package com.example.dreamscopebackend.repository;

import com.example.dreamscopebackend.entity.Dream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DreamRepository extends JpaRepository<Dream, UUID> {
    List<Dream> findByThreadUserUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Dream> findByDreamIdAndThreadUserUserId(UUID dreamId, UUID userId);

    List<Dream> findByThreadUserUserIdAndThreadThreadIdOrderByCreatedAtDesc(UUID userId, String threadId);

    void deleteByThreadUserUserId(UUID userId);
}
