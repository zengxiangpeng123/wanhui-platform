package com.wanhui.forum;

import com.wanhui.forum.mapper.CommentMapper;
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
class CommentLikeControllerTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    private Long commentId;
    private Long postId;

    @BeforeEach
    void prepareComment() throws Exception {
        postMapper.delete(null);
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
        userMapper.insert(testUser());

        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"评论点赞帖\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"正文\"}"))
                .andExpect(status().isCreated());

        postId = postMapper.selectList(null).get(0).getId();
        mockMvc.perform(post("/api/v1/forum/posts/" + postId + "/comments")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"待点赞评论\"}"))
                .andExpect(status().isCreated());
        commentId = commentMapper.selectList(null).get(0).getId();
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(put("/api/v1/forum/comments/" + commentId + "/like").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));
    }

    @Test
    void likingTwiceIsIdempotent() throws Exception {
        mockMvc.perform(put("/api/v1/forum/comments/" + commentId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        mockMvc.perform(put("/api/v1/forum/comments/" + commentId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));
    }

    @Test
    void unlikingNeverGoesNegative() throws Exception {
        mockMvc.perform(put("/api/v1/forum/comments/" + commentId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/forum/comments/" + commentId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0));

        mockMvc.perform(delete("/api/v1/forum/comments/" + commentId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(0));
    }

    @Test
    void commentListExposesLikeState() throws Exception {
        mockMvc.perform(put("/api/v1/forum/comments/" + commentId + "/like").with(user("1")).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/forum/posts/" + postId + "/comments").with(user("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].likeCount").value(1))
                .andExpect(jsonPath("$.data.items[0].liked").value(true));

        mockMvc.perform(get("/api/v1/forum/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].likeCount").value(1))
                .andExpect(jsonPath("$.data.items[0].liked").value(false));
    }

    @Test
    void likingMissingCommentReturns404() throws Exception {
        mockMvc.perform(put("/api/v1/forum/comments/999999/like").with(user("1")).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("comment_like_user");
        user.setUsernameNormalized("comment_like_user");
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext("cipher".getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac("comment-like-email".getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked("com***@example.com");
        user.setNickname("评论点赞用户");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}



