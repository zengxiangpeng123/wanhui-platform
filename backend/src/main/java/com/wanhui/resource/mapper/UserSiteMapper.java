package com.wanhui.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.resource.entity.UserSite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserSiteMapper extends BaseMapper<UserSite> {
    @Select("SELECT * FROM user_sites WHERE user_id = #{userId} ORDER BY created_at DESC, id DESC")
    List<UserSite> selectByUser(@Param("userId") Long userId);

    @Delete("DELETE FROM user_sites WHERE id = #{siteId} AND user_id = #{userId}")
    int deleteOwned(@Param("siteId") Long siteId, @Param("userId") Long userId);
}
