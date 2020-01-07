package com.transsnet.vskit.neptune.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * description：Swagger2配置
 *
 * @author yangwei
 * @date 2019-06-14 15:43
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        List<Parameter> params = new ArrayList<>();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.transsnet.vskit"))
                .paths(PathSelectors.any())
                .build().globalOperationParameters(params);
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("vskit-neptune-console 微服务 APIs")
                .version("1.0")
                .build();
    }
}