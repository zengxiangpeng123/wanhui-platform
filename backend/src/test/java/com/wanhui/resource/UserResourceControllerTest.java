package com.wanhui.resource;

import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "1", roles = "USER")
class UserResourceControllerTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void prepareUsers() {
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
        userMapper.insert(testUser(1L, "user_one"));
        userMapper.insert(testUser(2L, "user_two"));
    }

    @Test
    void favoriteIsIdempotentAndUnknownResourceReturns404() throws Exception {
        mockMvc.perform(put("/api/v1/resources/res-001/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/v1/resources/res-001/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/users/me/favorites/resources").with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].resourceKey").value("res-001"))
                .andExpect(jsonPath("$.data.items[0].available").value(true));

        mockMvc.perform(delete("/api/v1/resources/res-001/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/resources/res-001/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/v1/resources/not-found-key/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void recentVisitsKeepOnlyTwentyNewestDistinctResources() throws Exception {
        for (int i = 1; i <= 21; i++) {
            mockMvc.perform(post("/api/v1/users/me/recent-visits")
                            .with(user("1")).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resourceKey\":\"res-" + String.format("%03d", i) + "\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/users/me/recent-visits").with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(20));

        mockMvc.perform(post("/api/v1/users/me/recent-visits")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceKey\":\"res-001\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/users/me/recent-visits").with(user("1")).with(csrf()))
                .andExpect(status().isNoContent());

    }

    @Test
    void userSitesRejectDuplicateNormalizedUrlsAndAreUserScoped() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/sites")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Figma","url":"HTTPS://WWW.FIGMA.COM/"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Figma"))
                .andExpect(jsonPath("$.data.url").value("https://www.figma.com"));

        mockMvc.perform(post("/api/v1/users/me/sites")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Figma duplicate","url":"https://www.figma.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));

        mockMvc.perform(get("/api/v1/users/me/sites").with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @WithMockUser(username = "2", roles = "USER")
    void otherUserCannotSeeFirstUsersSites() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/sites").with(user("2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @WithAnonymousUser
    void unauthenticatedUserGets401() throws Exception {
        mockMvc.perform(put("/api/v1/resources/res-001/favorite").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));
    }

    private User testUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setUsernameNormalized(username);
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext("cipher".getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac(("email-" + username).getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked(username + "***@example.com");
        user.setNickname(username);
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
