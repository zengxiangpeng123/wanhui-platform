package com.wanhui.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Update("UPDATE post_comments SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrementLikeCount(@Param("commentId") Long commentId);

    @Update("UPDATE post_comments SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{commentId}")
    int decrementLikeCount(@Param("commentId") Long commentId);

    @Select("""
            SELECT c.* FROM post_comments c
            WHERE c.post_id = #{postId}
              AND c.status = 'PUBLISHED'
            ORDER BY c.created_at DESC, c.id DESC
            LIMIT #{offset}, #{size}
            """)
    List<Comment> selectPublishedByPost(
            @Param("postId") Long postId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Select("""
            SELECT COUNT(*) FROM post_comments c
            WHERE c.post_id = #{postId}
              AND c.status = 'PUBLISHED'
            """)
    long countPublishedByPost(@Param("postId") Long postId);
}


