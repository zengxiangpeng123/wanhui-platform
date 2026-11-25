package com.wanhui.forum;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithAnonymousUser
class InteractionControllerTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @BeforeEach
    void prepareUserAndPost() throws Exception {
        postMapper.delete(null);
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
        userMapper.insert(testUser());
        createPost();
    }

    @Test
    void requiresAuthenticationForLikeAndComment() throws Exception {
        Long postId = postId();

        mockMvc.perform(put("/api/v1/forum/posts/" + postId + "/like").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));

        mockMvc.perform(post("/api/v1/forum/posts/" + postId + "/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"匿名评论\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));
    }

    @Test
    void likingTwiceIsIdempotentAndKeepsCount() throws Exception {
        Long postId = postId();

        mockMvc.perform(put("/api/v1/forum/posts/" + postId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        mockMvc.perform(put("/api/v1/forum/posts/" + postId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));
    }

    @Test
    void unlikingRemovesLikeAndIsIdempotent() throws Exception {
        Long postId = postId();
        mockMvc.perform(put("/api/v1/forum/posts/" + postId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/forum/posts/" + postId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0));

        mockMvc.perform(delete("/api/v1/forum/posts/" + postId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0));
    }

    @Test
    void favoritingTwiceIsIdempotent() throws Exception {
        Long postId = postId();

        mockMvc.perform(put("/api/v1/forum/posts/" + postId + "/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(true))
                .andExpect(jsonPath("$.data.favoriteCount").value(1));

        mockMvc.perform(put("/api/v1/forum/posts/" + postId + "/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(true))
                .andExpect(jsonPath("$.data.favoriteCount").value(1));

        mockMvc.perform(delete("/api/v1/forum/posts/" + postId + "/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(false))
                .andExpect(jsonPath("$.data.favoriteCount").value(0));
    }

    @Test
    void rejectsEmptyAndOversizedComment() throws Exception {
        Long postId = postId();

        mockMvc.perform(post("/api/v1/forum/posts/" + postId + "/comments")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"  \"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        String oversized = "x".repeat(1001);
        mockMvc.perform(post("/api/v1/forum/posts/" + postId + "/comments")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + oversized + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void commentIncrementsCountAndAppearsInList() throws Exception {
        Long postId = postId();

        mockMvc.perform(post("/api/v1/forum/posts/" + postId + "/comments")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"第一条评论\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("第一条评论"))
                .andExpect(jsonPath("$.data.author.id").value("1"));

        mockMvc.perform(get("/api/v1/forum/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentCount").value(1))
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.favorited").value(false));

        mockMvc.perform(get("/api/v1/forum/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].content").value("第一条评论"));
    }

    @Test
    void returnsCommentStateForCurrentUserOnDetail() throws Exception {
        Long postId = postId();
        mockMvc.perform(put("/api/v1/forum/posts/" + postId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/forum/posts/" + postId + "/favorite").with(user("1")).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/forum/posts/" + postId).with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.favorited").value(true));
    }

    private Long postId() {
        return postMapper.selectList(null).get(0).getId();
    }

    private void createPost() throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"互动测试帖\",\"categoryCode\":\"academic\",\"tags\":[\"测试\"],\"content\":\"帖子正文内容\"}"))
                .andExpect(status().isCreated());
    }

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("interaction_user");
        user.setUsernameNormalized("interaction_user");
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext("cipher".getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac("interaction-email".getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked("inte***@example.com");
        user.setNickname("互动用户");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
