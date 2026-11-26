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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithAnonymousUser
class PostSearchControllerTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @BeforeEach
    void preparePosts() throws Exception {
        postMapper.delete(null);
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
        userMapper.insert(testUser());
    }

    @Test
    void matchesTitle() throws Exception {
        createPost("Figma 协作设计入门", "正文内容", "[\"设计\"]");
        mockMvc.perform(get("/api/v1/forum/posts/search").param("q", "Figma"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("Figma 协作设计入门"));
    }

    @Test
    void matchesContent() throws Exception {
        createPost("普通标题", "这里介绍 Obsidian 的双向链接", "[]");
        mockMvc.perform(get("/api/v1/forum/posts/search").param("q", "Obsidian"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void matchesTag() throws Exception {
        createPost("标签命中帖", "正文", "[\"文献管理\"]");
        mockMvc.perform(get("/api/v1/forum/posts/search").param("q", "文献管理"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void returnsEmptyForUnknownKeyword() throws Exception {
        createPost("任意帖子", "正文", "[]");
        mockMvc.perform(get("/api/v1/forum/posts/search").param("q", "no-such-keyword-xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void excludesSoftDeletedPosts() throws Exception {
        createPost("即将删除的搜索帖", "正文", "[]");
        Long postId = postMapper.selectList(null).get(0).getId();
        mockMvc.perform(delete("/api/v1/forum/posts/" + postId).with(user("1")).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/forum/posts/search").param("q", "即将删除"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    private void createPost(String title, String content, String tags) throws Exception {
        mockMvc.perform(post("/api/v1/forum/posts")
                        .with(user("1")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"categoryCode\":\"academic\",\"tags\":" + tags + ",\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated());
    }

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("search_user");
        user.setUsernameNormalized("search_user");
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext("cipher".getBytes(StandardCharsets.UTF_8));
        user.setEmailIv(new byte[12]);
        user.setEmailKeyVersion(1);
        user.setEmailHmac("search-email".getBytes(StandardCharsets.UTF_8));
        user.setEmailHmacVersion(1);
        user.setEmailMasked("sea***@example.com");
        user.setNickname("搜索测试用户");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
