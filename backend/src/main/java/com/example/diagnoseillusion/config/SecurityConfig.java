package com.example.diagnoseillusion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 前后端分离项目通常禁用 CSRF
            .csrf(csrf -> csrf.disable())
            // 启用 Spring MVC 提供的全局 CORS 配置 (会复用我们刚刚写的 CorsConfig)
            .cors(Customizer.withDefaults())
            // 权限验证配置
            .authorizeHttpRequests(auth -> auth
                // 暂时放行所有请求，方便前期接口开发联调
                // 后续可配置为例如：.requestMatchers("/api/public/**").permitAll().anyRequest().authenticated()
                .anyRequest().permitAll()
            );

        return http.build();
    }
}

