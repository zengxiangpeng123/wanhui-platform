package com.wanhui.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wanhuiOpenApi() {
        return new OpenAPI().info(new Info()
                .title("万汇网 API")
                .version("v1")
                .description("万汇网资源库与论坛 MVP 接口。统一响应结构："
                        + "成功 {success:true,data,meta?,traceId}；"
                        + "失败 {success:false,error:{code,message,fieldErrors?},traceId}。"));
    }
}
