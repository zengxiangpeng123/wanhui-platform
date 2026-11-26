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
import org.springframework.test.web.servlet.MvcResult;

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
class PostControllerTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @BeforeEach
    void prepareUserAndPosts() {
        postMapper.delete(null);
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
        userMapper.insert(testUser());
    }

    @Test
    void createsValidPostWithAuthorFromSession() throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "推荐几个好用的文献管理工具",
                                  "categoryCode": "academic",
                                  "tags": ["文献管理", "科研工具"],
                                  "content": "这是一篇帖子正文。"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("推荐几个好用的文献管理工具"))
                .andExpect(jsonPath("$.data.author.id").value("1"))
                .andExpect(jsonPath("$.data.tags.length()").value(2));
    }

    @Test
    void rejectsEmptyTitleAndOversizedContent() throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","categoryCode":"academic","tags":[],"content":"正文"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        String oversized = "x".repeat(20001);
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"过长正文\",\"categoryCode\":\"academic\",\"tags\":[],\"content\":\"" + oversized + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void listsPublishedPostsWithPagination() throws Exception {
        createPost("第一篇");
        createPost("第二篇");

        mockMvc.perform(get("/api/v1/forum/posts").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").isNotEmpty());
    }

    @Test
    void returns404ForMissingPost() throws Exception {
        mockMvc.perform(get("/api/v1/forum/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void softDeletesPostAndReturns410OnDetail() throws Exception {
        createPost("待删除帖子");
        Long postId = postMapper.selectList(null).get(0).getId();

        mockMvc.perform(delete("/api/v1/forum/posts/" + postId)
                        .with(user("1")).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/forum/posts/" + postId))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error.code").value("POST_DELETED"));
    }

    private MvcResult createPost(String title) throws Exception {
        return mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"categoryCode\":\"academic\",\"tags\":[\"测试\"],\"content\":\"帖子正文内容\"}"))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("forum_user");
        user.setUsernameNormalized("forum_user");
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext("cipher".getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac("forum-email".getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked("for***@example.com");
        user.setNickname("论坛用户");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
