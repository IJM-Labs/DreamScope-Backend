package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.AcceptTermsRequestDTO;
import com.example.dreamscopebackend.dto.response.TermsResponseDTO;
import com.example.dreamscopebackend.entity.TermsAndConditions;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.entity.UserTerms;
import com.example.dreamscopebackend.exception.UserNotFoundException;
import com.example.dreamscopebackend.repository.TermsAndConditionsRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import com.example.dreamscopebackend.repository.UserTermsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TermsService {
    private final TermsAndConditionsRepository termsRepository;
    private final UserTermsRepository userTermsRepository;
    private final UserRepository userRepository;

    public TermsService(
            TermsAndConditionsRepository termsRepository,
            UserTermsRepository userTermsRepository,
            UserRepository userRepository
    ) {
        this.termsRepository = termsRepository;
        this.userTermsRepository = userTermsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TermsResponseDTO getLatestTerms() {
        TermsAndConditions terms = termsRepository.findTopByOrderByCreatedAtDesc()
                .orElseGet(() -> {
                    TermsAndConditions created = new TermsAndConditions();
                    created.setVersion("1.0");
                    created.setContent("DreamScope behandler drømmedata fortroligt. Data gemmes krypteret og kan slettes af brugeren efter GDPR.");
                    return termsRepository.save(created);
                });
        return toResponse(terms);
    }

    @Transactional
    public void acceptTerms(UUID userId, AcceptTermsRequestDTO request) {
        UUID termsId = request.termsId();
        if (termsId == null) {
            termsId = getLatestTerms().termsId();
        }
        if (userTermsRepository.existsByUserUserIdAndTermsTermsId(userId, termsId)) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Bruger findes ikke"));
        TermsAndConditions terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new IllegalArgumentException("Vilkår findes ikke"));
        UserTerms userTerms = new UserTerms();
        userTerms.setUser(user);
        userTerms.setTerms(terms);
        userTermsRepository.save(userTerms);
    }

    private TermsResponseDTO toResponse(TermsAndConditions terms) {
        return new TermsResponseDTO(terms.getTermsId(), terms.getVersion(), terms.getContent(), terms.getCreatedAt());
    }
}
