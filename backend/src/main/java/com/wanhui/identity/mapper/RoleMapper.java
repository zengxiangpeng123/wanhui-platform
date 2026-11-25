package com.wanhui.identity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.identity.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("""
            SELECT r.* FROM roles r
            JOIN user_roles ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
            ORDER BY r.id
            """)
    List<Role> selectByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT p.code FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            JOIN user_roles ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId}
            ORDER BY p.code
            """)
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
