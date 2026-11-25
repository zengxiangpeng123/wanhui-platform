package com.wanhui.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.forum.entity.PostCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostCategoryMapper extends BaseMapper<PostCategory> {
    @Select("SELECT * FROM post_categories WHERE enabled = 1 ORDER BY sort_order, id")
    List<PostCategory> selectEnabled();

    @Select("SELECT * FROM post_categories WHERE code = #{code} LIMIT 1")
    PostCategory selectByCode(String code);
}
