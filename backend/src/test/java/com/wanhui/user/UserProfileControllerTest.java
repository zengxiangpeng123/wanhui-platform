package com.wanhui.user;

import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithAnonymousUser
class UserProfileControllerTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepareUser() {
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
        userMapper.insert(testUser());
    }

    @Test
    void returns401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));
    }

    @Test
    void returnsProfileWithReadOnlyMaskedContacts() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/profile").with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("profile_user"))
                .andExpect(jsonPath("$.data.nickname").value("资料用户"))
                .andExpect(jsonPath("$.data.emailMasked").value("pro***@example.com"))
                .andExpect(jsonPath("$.data.phoneMasked").doesNotExist());
    }

    @Test
    void updatesNicknameAndBio() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"新昵称","bio":"这是我的个人简介","interest":"前端开发"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.bio").value("这是我的个人简介"))
                .andExpect(jsonPath("$.data.interest").value("前端开发"));
    }

    @Test
    void rejectsOversizedNicknameAndBio() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + "x".repeat(31) + "\",\"bio\":\"ok\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"ok\",\"bio\":\"" + "x".repeat(501) + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void rejectsWrongOldPassword() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/password")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"wrong-password","newPassword":"NewPassw0rd!"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_OLD_PASSWORD"));
    }

    @Test
    void changesPasswordWithCorrectOldPassword() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/password")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"OldPassw0rd!","newPassword":"NewPassw0rd!"}
                                """))
                .andExpect(status().isNoContent());

        User stored = userMapper.selectById(1L);
        org.assertj.core.api.Assertions.assertThat(
                passwordEncoder.matches("NewPassw0rd!", stored.getPasswordHash())).isTrue();
    }

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("profile_user");
        user.setUsernameNormalized("profile_user");
        user.setPasswordHash(passwordEncoder.encode("OldPassw0rd!"));
        user.setEmailCiphertext("cipher".getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac("profile-email".getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked("pro***@example.com");
        user.setNickname("资料用户");
        user.setAvatarKey("avatar_01");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
