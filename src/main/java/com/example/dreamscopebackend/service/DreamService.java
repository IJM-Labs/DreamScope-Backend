package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.CreateDreamRequestDTO;
import com.example.dreamscopebackend.dto.response.DreamResponseDTO;
import com.example.dreamscopebackend.dto.response.InterpretationResponseDTO;
import com.example.dreamscopebackend.entity.Dream;
import com.example.dreamscopebackend.entity.Interpretation;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.exception.DreamNotFoundException;
import com.example.dreamscopebackend.exception.UserNotFoundException;
import com.example.dreamscopebackend.repository.DreamRepository;
import com.example.dreamscopebackend.repository.InterpretationRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DreamService {
    private final DreamRepository dreamRepository;
    private final InterpretationRepository interpretationRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final InterpretationService interpretationService;

    public DreamService(
            DreamRepository dreamRepository,
            InterpretationRepository interpretationRepository,
            UserRepository userRepository,
            EncryptionService encryptionService,
            InterpretationService interpretationService
    ) {
        this.dreamRepository = dreamRepository;
        this.interpretationRepository = interpretationRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.interpretationService = interpretationService;
    }

    @Transactional
    public DreamResponseDTO createDream(UUID userId, CreateDreamRequestDTO request) {
        String content = request.content();
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Drømmetekst mangler");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Bruger findes ikke"));
        Dream dream = new Dream();
        dream.setUser(user);
        dream.setContentEncrypted(encryptionService.encrypt(content));
        Dream savedDream = dreamRepository.save(dream);

        String interpretationText = interpretationService.interpret(content);
        Interpretation interpretation = new Interpretation();
        interpretation.setDream(savedDream);
        interpretation.setTextEncrypted(encryptionService.encrypt(interpretationText));
        Interpretation savedInterpretation = interpretationRepository.save(interpretation);
        savedDream.getInterpretations().add(savedInterpretation);

        return toResponse(savedDream);
    }

    @Transactional(readOnly = true)
    public List<DreamResponseDTO> getDreams(UUID userId) {
        return dreamRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DreamResponseDTO getDream(UUID userId, UUID dreamId) {
        return toResponse(findDreamForUser(userId, dreamId));
    }

    @Transactional
    public void deleteDream(UUID userId, UUID dreamId) {
        dreamRepository.delete(findDreamForUser(userId, dreamId));
    }

    private Dream findDreamForUser(UUID userId, UUID dreamId) {
        return dreamRepository.findByDreamIdAndUserUserId(dreamId, userId)
                .orElseThrow(() -> new DreamNotFoundException("Drøm findes ikke"));
    }

    private DreamResponseDTO toResponse(Dream dream) {
        return new DreamResponseDTO(
                dream.getDreamId(),
                encryptionService.decrypt(dream.getContentEncrypted()),
                dream.getCreatedAt(),
                dream.getInterpretations()
                        .stream()
                        .map(interpretation -> new InterpretationResponseDTO(
                                interpretation.getInterpretationId(),
                                encryptionService.decrypt(interpretation.getTextEncrypted()),
                                interpretation.getCreatedAt()
                        ))
                        .toList()
        );
    }
}
