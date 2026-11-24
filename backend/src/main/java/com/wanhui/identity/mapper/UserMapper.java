package com.wanhui.identity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.identity.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username_normalized = #{usernameNormalized} LIMIT 1")
    User selectByUsernameNormalized(@Param("usernameNormalized") String usernameNormalized);

    @Select("SELECT * FROM users WHERE email_hmac = #{emailHmac} LIMIT 1")
    User selectByEmailHmac(@Param("emailHmac") byte[] emailHmac);
}
