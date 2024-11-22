package com.SecondHand.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 이 클래스는 Spring 설정 파일임을 나타냄
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 자원 경로를 설정하여 클라이언트 요청을 해당 경로로 매핑
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/"); // '/js/**'로 시작하는 요청을 classpath의 'static/js/' 경로에 매핑

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/"); // '/css/**'로 시작하는 요청을 classpath의 'static/css/' 경로에 매핑

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/"); // '/images/**'로 시작하는 요청을 classpath의 'static/images/' 경로에 매핑
    }
}
