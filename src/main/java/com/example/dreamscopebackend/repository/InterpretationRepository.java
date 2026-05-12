package com.example.dreamscopebackend.repository;

import com.example.dreamscopebackend.entity.Interpretation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterpretationRepository extends JpaRepository<Interpretation, UUID> {
}
