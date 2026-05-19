package com.example.dreamscopebackend.controller;

import com.example.dreamscopebackend.entity.MagicLink;
import com.example.dreamscopebackend.entity.User;
import com.example.dreamscopebackend.repository.DreamRepository;
import com.example.dreamscopebackend.repository.DreamThreadRepository;
import com.example.dreamscopebackend.repository.InterpretationRepository;
import com.example.dreamscopebackend.repository.MagicLinkRepository;
import com.example.dreamscopebackend.repository.UserRepository;
import com.example.dreamscopebackend.repository.UserTermsRepository;
import com.example.dreamscopebackend.service.EncryptionService;
import com.example.dreamscopebackend.util.HashUtil;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DreamScopeApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MagicLinkRepository magicLinkRepository;

    @Autowired
    private UserTermsRepository userTermsRepository;

    @Autowired
    private DreamThreadRepository dreamThreadRepository;

    @Autowired
    private DreamRepository dreamRepository;

    @Autowired
    private InterpretationRepository interpretationRepository;

    @Test
    void loginEndpointCreatesUserAndOneTimeCodeRecord() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "api-user@example.com",
                                  "nickname": "Api User"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("One-time code sent if the email can receive mail."));

        User user = userRepository.findByEmailHash(HashUtil.sha256("api-user@example.com")).orElseThrow();
        assertThat(encryptionService.decrypt(user.getNicknameEncrypted())).isEqualTo("Api User");
        assertThat(magicLinkRepository.findAll())
                .singleElement()
                .satisfies(magicLink -> {
                    assertThat(magicLink.getUser().getUserId()).isEqualTo(user.getUserId());
                    assertThat(magicLink.getTokenHash()).hasSize(64);
                    assertThat(magicLink.isUsed()).isFalse();
                });
    }

    @Test
    void authenticatedUserCanAcceptTermsCreateDreamRenameThreadAndDeleteThread() throws Exception {
        User user = createUser("dream-flow@example.com", "Dream Flow");
        MockHttpSession session = verifiedSessionFor(user, "123456");

        mockMvc.perform(get("/api/terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.content").isNotEmpty());

        mockMvc.perform(post("/api/terms/accept-terms")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());
        assertThat(userTermsRepository.findAll()).hasSize(1);

        MvcResult createDream = mockMvc.perform(post("/api/dreams")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "I dreamed I was climbing Kilimanjaro.",
                                  "threadId": "thread-kilimanjaro"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.threadId").value("thread-kilimanjaro"))
                .andExpect(jsonPath("$.content").value("I dreamed I was climbing Kilimanjaro."))
                .andExpect(jsonPath("$.interpretations", hasSize(1)))
                .andExpect(jsonPath("$.interpretations[0].text").value("Demo interpretation: This dream may point to feelings, concerns, or wishes the user is processing. Add OPENAI_API_KEY for a real AI interpretation."))
                .andReturn();

        String dreamId = JsonPath.read(createDream.getResponse().getContentAsString(), "$.dreamId");
        assertThat(dreamThreadRepository.findByThreadIdAndUserUserId("thread-kilimanjaro", user.getUserId())).isPresent();

        mockMvc.perform(get("/api/dreams").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dreamId").value(dreamId))
                .andExpect(jsonPath("$[0].threadId").value("thread-kilimanjaro"));

        mockMvc.perform(put("/api/dreams/threads/thread-kilimanjaro/title")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Mountain dream"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/dreams/" + dreamId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Mountain dream"));

        mockMvc.perform(delete("/api/dreams/threads/thread-kilimanjaro").session(session))
                .andExpect(status().isNoContent());
        assertThat(dreamRepository.findAll()).isEmpty();
        assertThat(interpretationRepository.findAll()).isEmpty();
        assertThat(dreamThreadRepository.findByThreadIdAndUserUserId("thread-kilimanjaro", user.getUserId())).isEmpty();
    }

    @Test
    void deleteCurrentUserRemovesOwnedDataAndInvalidatesSession() throws Exception {
        User user = createUser("delete-flow@example.com", "Delete Flow");
        MockHttpSession session = verifiedSessionFor(user, "654321");

        mockMvc.perform(post("/api/dreams")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "I dreamed about an old house.",
                                  "threadId": "thread-house"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/terms/accept-terms")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/users/me").session(session))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(user.getUserId())).isEmpty();
        assertThat(dreamThreadRepository.findAll()).isEmpty();
        assertThat(dreamRepository.findAll()).isEmpty();
        assertThat(interpretationRepository.findAll()).isEmpty();
        assertThat(magicLinkRepository.findAll()).isEmpty();
        assertThat(userTermsRepository.findAll()).isEmpty();

        mockMvc.perform(get("/api/users/me").session(session))
                .andExpect(status().isForbidden());
    }

    private MockHttpSession verifiedSessionFor(User user, String code) throws Exception {
        MagicLink magicLink = new MagicLink();
        magicLink.setUser(user);
        magicLink.setTokenHash(HashUtil.sha256(code));
        magicLink.setExpiresAt(Instant.now().plusSeconds(600));
        magicLinkRepository.save(magicLink);

        MvcResult result = mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getUserId().toString()))
                .andExpect(jsonPath("$.message").value("Login verified"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private User createUser(String email, String nickname) {
        User user = new User();
        user.setEmailHash(HashUtil.sha256(email));
        user.setEmailEncrypted(encryptionService.encrypt(email));
        user.setNicknameEncrypted(encryptionService.encrypt(nickname));
        return userRepository.save(user);
    }

}
