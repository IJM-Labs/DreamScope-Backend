package com.example.dreamscopebackend.repository;

import com.example.dreamscopebackend.entity.TermsAndConditions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditions, UUID> {
    Optional<TermsAndConditions> findTopByOrderByCreatedAtDesc();
}
