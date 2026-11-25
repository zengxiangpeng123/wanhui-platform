package com.wanhui.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSiteRequest(
        @NotBlank(message = "网站名称不能为空")
        @Size(max = 100, message = "网站名称不能超过 100 个字符")
        String name,
        @NotBlank(message = "网站地址不能为空")
        @Size(max = 500, message = "网站地址不能超过 500 个字符")
        String url,
        @Size(max = 500, message = "图标地址不能超过 500 个字符")
        String iconUrl
) {
}
