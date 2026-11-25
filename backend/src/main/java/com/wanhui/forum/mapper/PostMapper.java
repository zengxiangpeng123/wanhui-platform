package com.wanhui.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.forum.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    @Select("""
            SELECT p.* FROM posts p
            WHERE p.status = 'PUBLISHED'
              AND (#{categoryCode} IS NULL OR p.category_id = (
                  SELECT c.id FROM post_categories c WHERE c.code = #{categoryCode}
              ))
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT #{offset}, #{size}
            """)
    List<Post> selectPublished(
            @Param("categoryCode") String categoryCode,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Select("""
            SELECT COUNT(*) FROM posts p
            WHERE p.status = 'PUBLISHED'
              AND (#{categoryCode} IS NULL OR p.category_id = (
                  SELECT c.id FROM post_categories c WHERE c.code = #{categoryCode}
              ))
            """)
    long countPublished(@Param("categoryCode") String categoryCode);


    @Select("""
            SELECT p.* FROM posts p
            WHERE p.status <> 'DELETED'
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT #{offset}, #{size}
            """)
    List<Post> selectForAdmin(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM posts p WHERE p.status <> 'DELETED'")
    long countForAdmin();
    @Update("UPDATE posts SET like_count = like_count + 1 WHERE id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId);

    @Update("UPDATE posts SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{postId}")
    int decrementLikeCount(@Param("postId") Long postId);

    @Update("UPDATE posts SET favorite_count = favorite_count + 1 WHERE id = #{postId}")
    int incrementFavoriteCount(@Param("postId") Long postId);

    @Update("UPDATE posts SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE id = #{postId}")
    int decrementFavoriteCount(@Param("postId") Long postId);

    @Update("UPDATE posts SET comment_count = comment_count + 1 WHERE id = #{postId}")
    int incrementCommentCount(@Param("postId") Long postId);
}

