package com.wanhui;

import com.wanhui.identity.mapper.RoleMapper;
import com.wanhui.identity.mapper.UserMapper;
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

    @Test
    void contextLoads() {
    }
}
