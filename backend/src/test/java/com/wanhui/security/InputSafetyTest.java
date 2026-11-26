package com.wanhui.security;

import com.wanhui.forum.mapper.PostMapper;
import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithAnonymousUser
class InputSafetyTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @BeforeEach
    void prepareUser() {
        postMapper.delete(null);
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
        userMapper.insert(testUser());
    }

    @Test
    void overlongTitleIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "标".repeat(101) + "\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"正文\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void overlongContentIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"超长正文\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"" + "x".repeat(20001) + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void overlongCommentIsRejected() throws Exception {
        Long postId = createPost();
        mockMvc.perform(post("/api/v1/forum/posts/" + postId + "/comments")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + "x".repeat(1001) + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void sqlSpecialCharactersAreStoredSafely() throws Exception {
        String title = "1' OR '1'='1";
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"正文\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value(title));
    }

    @Test
    void duplicateRegistrationReturnsConflict() throws Exception {
        String body = "{\"username\":\"dup_user\",\"email\":\"dup@example.com\",\"password\":\"Passw0rd!\"}";
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void unauthenticatedWriteIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"未登录发帖\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"正文\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));
    }

    private Long createPost() throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"评论测试帖\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"正文\"}"))
                .andExpect(status().isCreated());
        return postMapper.selectList(null).get(0).getId();
    }

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("input_user");
        user.setUsernameNormalized("input_user");
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext("cipher".getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac("input-email".getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked("inp***@example.com");
        user.setNickname("输入测试用户");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
