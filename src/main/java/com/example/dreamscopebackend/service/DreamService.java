package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.dto.request.CreateDreamRequestDTO;
import com.example.dreamscopebackend.dto.request.UpdateThreadTitleRequestDTO;
import com.example.dreamscopebackend.dto.response.DreamResponseDTO;
import com.example.dreamscopebackend.dto.response.InterpretationResponseDTO;
import com.example.dreamscopebackend.entity.Dream;
import com.example.dreamscopebackend.entity.DreamThread;
import com.example.dreamscopebackend.entity.Interpretation;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.exception.DreamNotFoundException;
import com.example.dreamscopebackend.exception.UserNotFoundException;
import com.example.dreamscopebackend.repository.DreamRepository;
import com.example.dreamscopebackend.repository.DreamThreadRepository;
import com.example.dreamscopebackend.repository.InterpretationRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DreamService {
    private final DreamRepository dreamRepository;
    private final DreamThreadRepository dreamThreadRepository;
    private final InterpretationRepository interpretationRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final InterpretationService interpretationService;

    public DreamService(
            DreamRepository dreamRepository,
            DreamThreadRepository dreamThreadRepository,
            InterpretationRepository interpretationRepository,
            UserRepository userRepository,
            EncryptionService encryptionService,
            InterpretationService interpretationService
    ) {
        this.dreamRepository = dreamRepository;
        this.dreamThreadRepository = dreamThreadRepository;
        this.interpretationRepository = interpretationRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.interpretationService = interpretationService;
    }

    @Transactional
    public DreamResponseDTO createDream(UUID userId, CreateDreamRequestDTO request) {
        String content = request.content();
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Dream text is required");
        }
        String threadId = normalizeThreadId(request.threadId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User was not found"));
        DreamThread thread = findOrCreateThread(user, threadId);
        Dream dream = new Dream();
        dream.setThread(thread);
        dream.setContentEncrypted(encryptionService.encrypt(content));
        Dream savedDream = dreamRepository.save(dream);

        String interpretationText = interpretationService.interpret(content);
        if (thread.getTitleEncrypted() == null || thread.getTitleEncrypted().isBlank()) {
            thread.setTitleEncrypted(encryptionService.encrypt(interpretationService.titleFor(content, interpretationText)));
        }
        Interpretation interpretation = new Interpretation();
        interpretation.setDream(savedDream);
        interpretation.setTextEncrypted(encryptionService.encrypt(interpretationText));
        Interpretation savedInterpretation = interpretationRepository.save(interpretation);
        savedDream.getInterpretations().add(savedInterpretation);

        return toResponse(savedDream);
    }

    @Transactional(readOnly = true)
    public List<DreamResponseDTO> getDreams(UUID userId) {
        return dreamRepository.findByThreadUserUserIdOrderByCreatedAtDesc(userId)
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

    @Transactional
    public void deleteThread(UUID userId, String threadId) {
        DreamThread thread = threadForUser(userId, threadId);
        dreamRepository.deleteAll(dreamRepository.findByThreadUserUserIdAndThreadThreadIdOrderByCreatedAtDesc(userId, threadId));
        dreamThreadRepository.delete(thread);
    }

    @Transactional
    public void updateThreadTitle(UUID userId, String threadId, UpdateThreadTitleRequestDTO request) {
        String title = request.title();
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Thread title is required");
        }

        String cleanedTitle = title.trim();
        if (cleanedTitle.length() > 80) {
            cleanedTitle = cleanedTitle.substring(0, 80).trim();
        }

        String encryptedTitle = encryptionService.encrypt(cleanedTitle);
        threadForUser(userId, threadId).setTitleEncrypted(encryptedTitle);
    }

    private Dream findDreamForUser(UUID userId, UUID dreamId) {
        return dreamRepository.findByDreamIdAndThreadUserUserId(dreamId, userId)
                .orElseThrow(() -> new DreamNotFoundException("Dream was not found"));
    }

    private DreamResponseDTO toResponse(Dream dream) {
        String content = encryptionService.decrypt(dream.getContentEncrypted());
        return new DreamResponseDTO(
                dream.getDreamId(),
                dream.getThreadId(),
                titleFor(dream.getThread(), content),
                content,
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

    private DreamThread findOrCreateThread(User user, String threadId) {
        return dreamThreadRepository.findByThreadIdAndUserUserId(threadId, user.getUserId())
                .orElseGet(() -> {
                    DreamThread thread = new DreamThread();
                    thread.setThreadId(threadId);
                    thread.setUser(user);
                    return dreamThreadRepository.save(thread);
                });
    }

    private DreamThread threadForUser(UUID userId, String threadId) {
        if (threadId == null || threadId.isBlank()) {
            throw new IllegalArgumentException("Thread id is required");
        }
        return dreamThreadRepository.findByThreadIdAndUserUserId(threadId, userId)
                .orElseThrow(() -> new DreamNotFoundException("Dream thread was not found"));
    }

    private String titleFor(DreamThread thread, String content) {
        if (thread.getTitleEncrypted() != null && !thread.getTitleEncrypted().isBlank()) {
            return encryptionService.decrypt(thread.getTitleEncrypted());
        }
        String fallback = content == null ? "" : content.trim().split("[.!?\\n]")[0].trim();
        if (fallback.isBlank()) {
            return "New dream chat";
        }
        return fallback.length() > 42 ? fallback.substring(0, 42).trim() : fallback;
    }

    private String normalizeThreadId(String threadId) {
        if (threadId == null || threadId.isBlank()) {
            return "thread-" + UUID.randomUUID();
        }
        String cleaned = threadId.trim();
        return cleaned.length() > 80 ? cleaned.substring(0, 80) : cleaned;
    }
}
