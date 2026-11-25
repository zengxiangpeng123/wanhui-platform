package com.wanhui.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.forum.entity.PostFavorite;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {
    @Insert("""
            INSERT IGNORE INTO post_favorites (post_id, user_id)
            VALUES (#{postId}, #{userId})
            """)
    int insertIgnore(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*) FROM post_favorites
            WHERE post_id = #{postId} AND user_id = #{userId}
            """)
    long countByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);
}
