package com.wanhui.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.forum.entity.CommentLike;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    @Insert("""
            INSERT IGNORE INTO comment_likes (comment_id, user_id)
            VALUES (#{commentId}, #{userId})
            """)
    int insertIgnore(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*) FROM comment_likes
            WHERE comment_id = #{commentId} AND user_id = #{userId}
            """)
    long countByCommentAndUser(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
