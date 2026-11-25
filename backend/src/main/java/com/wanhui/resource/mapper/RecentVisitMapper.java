package com.wanhui.resource.mapper;

import com.wanhui.resource.entity.RecentVisit;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecentVisitMapper {
    @Insert("""
            INSERT INTO recent_visits (user_id, resource_key, visited_at)
            VALUES (#{userId}, #{resourceKey}, CURRENT_TIMESTAMP(3))
            ON DUPLICATE KEY UPDATE visited_at = CURRENT_TIMESTAMP(3)
            """)
    int upsert(@Param("userId") Long userId, @Param("resourceKey") String resourceKey);

    @Select("SELECT * FROM recent_visits WHERE user_id = #{userId} ORDER BY visited_at DESC")
    List<RecentVisit> selectAllByUser(@Param("userId") Long userId);

    @Delete("DELETE FROM recent_visits WHERE user_id = #{userId} AND resource_key = #{resourceKey}")
    int deleteOne(@Param("userId") Long userId, @Param("resourceKey") String resourceKey);

    @Delete("DELETE FROM recent_visits WHERE user_id = #{userId}")
    int clear(@Param("userId") Long userId);
}
