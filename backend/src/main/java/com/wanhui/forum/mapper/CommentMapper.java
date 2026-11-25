package com.wanhui.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
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
