package com.wanhui.identity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.identity.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username_normalized = #{usernameNormalized} LIMIT 1")
    User selectByUsernameNormalized(@Param("usernameNormalized") String usernameNormalized);

    @Select("SELECT * FROM users WHERE email_hmac = #{emailHmac} LIMIT 1")
    User selectByEmailHmac(@Param("emailHmac") byte[] emailHmac);

    @Select("""
            SELECT * FROM users
            WHERE id > #{afterId}
              AND (
                email_key_version < #{currentVersion}
                OR (phone_ciphertext IS NOT NULL AND phone_key_version < #{currentVersion})
              )
            ORDER BY id
            LIMIT #{limit}
            """)
    List<User> selectOutdatedKeyVersionAfter(
            @Param("currentVersion") int currentVersion,
            @Param("afterId") long afterId,
            @Param("limit") int limit
    );
    @Delete("DELETE FROM user_roles")
    int deleteAllUserRoles();

    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int assignRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}


