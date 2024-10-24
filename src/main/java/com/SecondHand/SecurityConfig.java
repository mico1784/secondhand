package com.SecondHand;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository(){
        HttpSessionCsrfTokenRepository repository =
                new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 보호 비활성화 (개발 시 유용하지만, 실제 서비스에서는 주의)
        http.csrf((csrf) -> csrf.disable());

        // 요청 인증 설정
        http.authorizeHttpRequests((authorize) ->
                authorize
                        //.requestMatchers("/login", "/register","/add", "/home","/css/**").permitAll() // 로그인, 회원가입, 루트, 홈 URL은 인증 없이 접근 가능
                        //.anyRequest().authenticated() // 그 외의 요청은 인증 필요
                        .anyRequest().permitAll() // 모든 요청을 인증 없이 허용
        );

        // 로그인 설정
        http.formLogin((formLogin) ->
                formLogin.loginPage("/login")
                        .defaultSuccessUrl("/home") // 로그인 성공 후 이동할 페이지
                        .permitAll() // 로그인 페이지는 누구나 접근 가능
        );

        // 로그아웃 설정
        http.logout(logout ->
                logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/home") // 로그아웃 후 이동할 페이지
                        .permitAll() // 로그아웃 페이지는 누구나 접근 가능
        );

        return http.build();
    }
}

