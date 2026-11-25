package com.wanhui.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.forum.entity.PostLike;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
    @Insert("""
            INSERT IGNORE INTO post_likes (post_id, user_id)
            VALUES (#{postId}, #{userId})
            """)
    int insertIgnore(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*) FROM post_likes
            WHERE post_id = #{postId} AND user_id = #{userId}
            """)
    long countByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);
}
