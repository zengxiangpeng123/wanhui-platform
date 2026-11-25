package com.wanhui;

import com.wanhui.identity.mapper.RoleMapper;
import com.wanhui.identity.mapper.UserMapper;
import com.wanhui.forum.mapper.PostCategoryMapper;
import com.wanhui.forum.mapper.CommentMapper;
import com.wanhui.forum.mapper.PostFavoriteMapper;
import com.wanhui.forum.mapper.PostLikeMapper;
import com.wanhui.forum.mapper.PostMapper;
import com.wanhui.forum.mapper.PostTagMapper;
import com.wanhui.forum.mapper.TagMapper;
import com.wanhui.resource.mapper.RecentVisitMapper;
import com.wanhui.resource.mapper.ResourceFavoriteMapper;
import com.wanhui.resource.mapper.UserSiteMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
class WanhuiApplicationTest {

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private RoleMapper roleMapper;

    @MockitoBean
    private ResourceFavoriteMapper resourceFavoriteMapper;

    @MockitoBean
    private RecentVisitMapper recentVisitMapper;

    @MockitoBean
    private UserSiteMapper userSiteMapper;

    @MockitoBean
    private PostMapper postMapper;

    @MockitoBean
    private PostCategoryMapper postCategoryMapper;

    @MockitoBean
    private TagMapper tagMapper;

    @MockitoBean
    private PostTagMapper postTagMapper;

    @MockitoBean
    private CommentMapper commentMapper;

    @MockitoBean
    private PostLikeMapper postLikeMapper;

    @MockitoBean
    private PostFavoriteMapper postFavoriteMapper;

    @Test
    void contextLoads() {
    }
}

