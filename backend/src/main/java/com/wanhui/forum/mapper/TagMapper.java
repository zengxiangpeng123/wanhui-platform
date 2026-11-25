package com.wanhui.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.forum.entity.Tag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Insert("INSERT IGNORE INTO tags (name, normalized_name) VALUES (#{name}, #{normalizedName})")
    int insertIgnore(Tag tag);

    @Select("SELECT * FROM tags WHERE normalized_name = #{normalizedName} LIMIT 1")
    Tag selectByNormalizedName(String normalizedName);

    @Select("""
            SELECT t.* FROM tags t
            JOIN post_tags pt ON pt.tag_id = t.id
            WHERE pt.post_id = #{postId}
            ORDER BY t.id
            """)
    List<Tag> selectByPostId(Long postId);
}
