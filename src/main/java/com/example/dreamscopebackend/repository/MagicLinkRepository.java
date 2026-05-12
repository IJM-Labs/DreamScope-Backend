package com.example.dreamscopebackend.repository;

import com.example.dreamscopebackend.entity.MagicLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MagicLinkRepository extends JpaRepository<MagicLink, UUID> {
    Optional<MagicLink> findByTokenHash(String tokenHash);

    void deleteByUserUserId(UUID userId);
}
