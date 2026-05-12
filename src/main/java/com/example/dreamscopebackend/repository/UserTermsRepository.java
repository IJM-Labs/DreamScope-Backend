package com.example.dreamscopebackend.repository;

import com.example.dreamscopebackend.entity.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserTermsRepository extends JpaRepository<UserTerms, UUID> {
    boolean existsByUserUserIdAndTermsTermsId(UUID userId, UUID termsId);

    void deleteByUserUserId(UUID userId);
}
