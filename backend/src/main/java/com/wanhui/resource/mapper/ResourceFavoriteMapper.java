package com.wanhui.resource.mapper;

import com.wanhui.resource.entity.ResourceFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResourceFavoriteMapper {
    @Insert("INSERT IGNORE INTO resource_favorites (user_id, resource_key) VALUES (#{userId}, #{resourceKey})")
    int insertIgnore(@Param("userId") Long userId, @Param("resourceKey") String resourceKey);

    @Delete("DELETE FROM resource_favorites WHERE user_id = #{userId} AND resource_key = #{resourceKey}")
    int deleteOne(@Param("userId") Long userId, @Param("resourceKey") String resourceKey);

    @Select("SELECT * FROM resource_favorites WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<ResourceFavorite> selectPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM resource_favorites WHERE user_id = #{userId}")
    long countByUser(@Param("userId") Long userId);
}
