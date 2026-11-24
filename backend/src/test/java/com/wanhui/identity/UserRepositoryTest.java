package com.wanhui.identity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wanhui.identity.entity.Role;
import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.RoleMapper;
import com.wanhui.identity.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserRepositoryTest extends IdentityDatabaseTestSupport {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @BeforeEach
    void cleanUsers() {
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
    }

    @Test
    void insertsAndLooksUpUserByNormalizedUsernameAndEmailHmac() {
        byte[] emailHmac = hmac("lookup@example.com");
        User user = user("lookup_user", "lookup@example.com", emailHmac);

        assertThat(userMapper.insert(user)).isEqualTo(1);
        assertThat(user.getId()).isNotNull();

        User byUsername = userMapper.selectByUsernameNormalized("lookup_user");
        User byEmail = userMapper.selectByEmailHmac(emailHmac);

        assertThat(byUsername).isNotNull();
        assertThat(byUsername.getId()).isEqualTo(user.getId());
        assertThat(byEmail).isNotNull();
        assertThat(byEmail.getId()).isEqualTo(user.getId());
    }

    @Test
    void rejectsDuplicateNormalizedUsername() {
        userMapper.insert(user("duplicate_name", "first@example.com", hmac("first@example.com")));

        assertThatThrownBy(() -> userMapper.insert(
                user("duplicate_name", "second@example.com", hmac("second@example.com"))
        )).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void rejectsDuplicateEmailHmac() {
        byte[] sharedHmac = hmac("shared@example.com");
        userMapper.insert(user("first_user", "shared@example.com", sharedHmac));

        assertThatThrownBy(() -> userMapper.insert(
                user("second_user", "other@example.com", sharedHmac)
        )).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void seedsUserAndAdminRoles() {
        Role userRole = roleMapper.selectOne(new QueryWrapper<Role>().eq("code", "USER"));
        Role adminRole = roleMapper.selectOne(new QueryWrapper<Role>().eq("code", "ADMIN"));

        assertThat(userRole).isNotNull();
        assertThat(adminRole).isNotNull();
    }

    private User user(String username, String email, byte[] emailHmac) {
        User user = new User();
        user.setUsername(username);
        user.setUsernameNormalized(username.toLowerCase());
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext(bytes(32, 1));
        user.setEmailIv(bytes(12, 2));
        user.setEmailKeyVersion(1);
        user.setEmailHmac(emailHmac);
        user.setEmailHmacVersion(1);
        user.setEmailMasked("lo***@example.com");
        user.setNickname(username);
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private byte[] hmac(String value) {
        byte[] digest = new byte[32];
        byte[] source = value.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < digest.length; i++) {
            digest[i] = source[i % source.length];
        }
        return digest;
    }

    private byte[] bytes(int size, int seed) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) (seed + i);
        }
        return result;
    }
}
