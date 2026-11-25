package com.wanhui.admin;

import com.wanhui.admin.mapper.AuditLogMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithAnonymousUser
class AdminPostControllerTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @BeforeEach
    void prepareData() throws Exception {
        auditLogMapper.delete(null);
        postMapper.delete(null);
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
        userMapper.insert(memberUser(1L, "member_user"));
        userMapper.insert(memberUser(2L, "admin_user"));
        createPost(1L);
    }

    @Test
    void normalUserCannotListOrDeleteThroughAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts").with(user("1").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(delete("/api/v1/admin/posts/" + postId())
                        .with(user("1").roles("USER")).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    void adminListsPosts() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts")
                        .with(user("2").roles("ADMIN").authorities(
                                () -> "POST_DELETE_ANY",
                                () -> "ADMIN_CONTENT_VIEW")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("待审核帖子"));
    }

    @Test
    void adminDeletesPostAndWritesAuditRecord() throws Exception {
        Long postId = postId();

        mockMvc.perform(delete("/api/v1/admin/posts/" + postId)
                        .with(user("2").roles("ADMIN").authorities(() -> "POST_DELETE_ANY")).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/forum/posts/" + postId))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error.code").value("POST_DELETED"));

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .with(user("2").roles("ADMIN").authorities(() -> "ADMIN_CONTENT_VIEW")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].operatorId").value("2"))
                .andExpect(jsonPath("$.data[0].targetId").value(String.valueOf(postId)))
                .andExpect(jsonPath("$.data[0].action").value("POST_DELETE"))
                .andExpect(jsonPath("$.data[0].result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].occurredAt").isNotEmpty());
    }

    @Test
    void deletingMissingPostReturns404() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/posts/999999")
                        .with(user("2").roles("ADMIN").authorities(() -> "POST_DELETE_ANY")).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    private Long postId() {
        return postMapper.selectList(null).get(0).getId();
    }

    private void createPost(Long authorId) throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user(String.valueOf(authorId)).roles("USER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"待审核帖子\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"正文内容\"}"))
                .andExpect(status().isCreated());
    }

    private User memberUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setUsernameNormalized(username);
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext(("cipher-" + id).getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac(("email-" + id).getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked("user***@example.com");
        user.setNickname(username);
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}


