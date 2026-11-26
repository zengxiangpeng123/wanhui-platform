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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithAnonymousUser
class SecurityHardeningTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

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
    void missingCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"无 CSRF\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"正文\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    void normalUserCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts").with(user("1").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    void illegalUrlSchemeIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/sites")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"恶意站点\",\"url\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void scriptInputIsReturnedAsJsonNotHtml() throws Exception {
        String payload = "<script>alert('xss')</script>";
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"XSS 测试\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":"
                                + "\"" + payload.replace("\"", "\\\"") + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(jsonPath("$.data.content").value(payload));
    }

    @Test
    void passwordIsStoredAsBcryptHashNotPlaintext() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"secure_user\",\"email\":\"secure@example.com\",\"password\":\"PlainPassw0rd!\"}"))
                .andExpect(status().isCreated());

        User stored = userMapper.selectByUsernameNormalized("secure_user");
        assertThat(stored).isNotNull();
        assertThat(stored.getPasswordHash()).isNotEqualTo("PlainPassw0rd!");
        assertThat(stored.getPasswordHash()).startsWith("$2");
    }

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("security_user");
        user.setUsernameNormalized("security_user");
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext("cipher".getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac("security-email".getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked("sec***@example.com");
        user.setNickname("安全测试用户");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
