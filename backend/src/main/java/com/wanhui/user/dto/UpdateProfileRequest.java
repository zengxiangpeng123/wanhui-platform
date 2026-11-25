package com.wanhui.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 30, message = "昵称不能超过 30 个字符")
        @Pattern(regexp = "^(?!\\s*$).*$", message = "昵称不能为空")
        String nickname,

        @Size(max = 500, message = "个人简介不能超过 500 个字符")
        String bio,

        @Size(max = 100, message = "兴趣方向不能超过 100 个字符")
        String interest,

        @Pattern(regexp = "^(avatar_(0[1-9]|1[0-2]))?$", message = "头像编号无效")
        String avatarKey
) {
}
