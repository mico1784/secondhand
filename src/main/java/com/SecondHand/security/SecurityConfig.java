package com.SecondHand.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    @Qualifier("myUserDetailsService") // 커스텀 UserDetailsService 빈 주입
    private UserDetailsService userDetailsService;

    @Autowired
    private DataSource dataSource; // JDBC 기반의 토큰 저장소를 위한 데이터소스 주입

    @Lazy
    @Autowired
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService; // OAuth2 사용자 서비스 주입

    // 비밀번호 암호화를 위한 Bean 설정 (BCrypt 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DaoAuthenticationProvider 설정
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // 사용자 세부정보 서비스 설정
        authProvider.setPasswordEncoder(passwordEncoder()); // 비밀번호 암호화 설정
        return authProvider;
    }

    // 인증 관리자 설정
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Spring Security 필터 체인 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider()) // 인증 제공자 설정
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth/kakao/**", "/oauth2/authorization/google", "/login", "/register", "/css/**", "/images/**", "/home", "/", "/add", "/js/**", "/list", "/list/**", "/home/categoryItems", "/filterItems", "/check-username", "/error").permitAll() // 특정 경로는 모든 사용자 허용
                        .requestMatchers("/item", "/item/edit/**", "/item/delete/**").authenticated() // 특정 경로는 인증된 사용자만 허용
                        .requestMatchers("/item/**").permitAll() // 나머지 아이템 경로는 모두 허용
                        .requestMatchers("/mypage").authenticated() // 마이페이지는 인증된 사용자만 허용
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/login") // 사용자 정의 로그인 페이지 설정
                        .defaultSuccessUrl("/home", false) // 로그인 성공 시 리다이렉트 경로 설정
                        .permitAll() // 로그인 페이지 모든 사용자 허용
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // OAuth2 로그인 페이지 설정
                        .defaultSuccessUrl("/home", false) // OAuth2 로그인 성공 시 리다이렉트 경로 설정
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // 사용자 정보 서비스 설정
                )
                .rememberMe(rememberMe -> rememberMe
                        .key("newUniqueRememberMeKey") // 고유한 키 설정
                        .rememberMeParameter("stayLoggedIn") // 파라미터 이름 설정
                        .userDetailsService(userDetailsService) // 사용자 세부정보 서비스 설정
                        .tokenRepository(persistentTokenRepository()) // 토큰 저장소 설정
                        .tokenValiditySeconds(60 * 60 * 24 * 30) // 토큰 유효 기간 (30일)
                        .useSecureCookie(false) // Secure 쿠키 설정 비활성화
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 URL 설정
                        .logoutSuccessUrl("/home") // 로그아웃 성공 후 리다이렉트 경로
                        .permitAll() // 로그아웃 페이지 모든 사용자 허용
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 세션 정책 설정
                        .maximumSessions(1) // 최대 세션 수 제한
                        .expiredUrl("/login?expired") // 세션 만료 시 리다이렉트 경로
                );

        return http.build(); // 설정된 HttpSecurity 객체 빌드
    }

    // 토큰 저장소 설정 (JDBC 기반)
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource); // 데이터소스 설정
        return tokenRepository;
    }

    // RememberMe 서비스 설정
    @Bean
    public RememberMeServices rememberMeServices() {
        PersistentTokenBasedRememberMeServices rememberMeServices =
                new PersistentTokenBasedRememberMeServices("uniqueAndSecret", userDetailsService, persistentTokenRepository());
        rememberMeServices.setAlwaysRemember(true); // 항상 RememberMe 사용
        rememberMeServices.setTokenValiditySeconds(86400); // 1일 동안 유효
        return rememberMeServices;
    }
}
