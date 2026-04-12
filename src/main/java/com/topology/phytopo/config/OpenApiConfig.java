package com.topology.phytopo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:phytopo}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("拓扑服务 API")
                        .version("1.0.0")
                        .description("物理拓扑服务 - 提供子网管理、网元管理、拓扑视图等功能")
                        .contact(new Contact()
                                .name("Topology Team")
                                .email("topology@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
