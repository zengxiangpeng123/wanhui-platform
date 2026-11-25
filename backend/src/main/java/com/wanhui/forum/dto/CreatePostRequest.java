package com.wanhui.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 100, message = "标题不能超过 100 个字符")
        String title,
        @NotBlank(message = "分类不能为空")
        String categoryCode,
        @Size(max = 5, message = "标签最多 5 个")
        List<@NotBlank @Size(max = 20, message = "每个标签不能超过 20 个字符") String> tags,
        @NotBlank(message = "正文不能为空")
        @Size(max = 20000, message = "正文不能超过 20000 个字符")
        String content
) {
    public CreatePostRequest {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
