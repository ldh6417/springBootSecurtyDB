package com.zeus.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.zeus.common.security.CustomAccessDeniedHandler;
import com.zeus.common.security.CustomLoginSuccessHandler;
import com.zeus.common.security.CustomNoOpPasswordEncoder;
import com.zeus.common.security.CustomUserDetailsService;

import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	DataSource dataSource;

	@Bean
	SecurityFilterChain FilterChain(HttpSecurity httpSecurity) throws Exception {
		log.info("securty config");

		// 1. csrf 토큰 비활성화
		httpSecurity.csrf((csrf) -> csrf.disable());

		// 2.인가정책
		httpSecurity.authorizeHttpRequests(auth -> auth.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
				.requestMatchers("/accessError", "/login", "/css/", "/js/", "/error").permitAll()
				.requestMatchers("/board/list").permitAll() // 게시판 목록: 누구나
				.requestMatchers("/board/register").hasRole("MEMBER") // 게시판 등록: 회원만
				.requestMatchers("/notice/list").permitAll() // 공지사항 목록: 누구나
				.requestMatchers("/notice/register").hasRole("ADMIN") // 공지사항 등록: 관리자만
				.anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
		);

		// 3.접근거부시 예외처리 설정 (/accessError 페이지로 이동)
		// httpSecurity.exceptionHandling(exception ->
		// exception.accessDeniedPage("/accessError"));

		// 등록한 CustomAccessDeniedHandler.java를 접근 거부 처리자로 지정한다.
		httpSecurity.exceptionHandling(exception -> exception.accessDeniedHandler(createAccessDeniedHandler()));

		// 4.기본폼 로그인을 활성화
		// httpSecurity.formLogin(Customizer.withDefaults());
		httpSecurity.formLogin(form -> form.loginPage("/login") // 커스텀 로그인 페이지 URL
				.loginProcessingUrl("/login")
				// .defaultSuccessUrl("/board/list") //성공시 기본 화면 설정
				.successHandler(createAuthenticationSuccessHandler()).permitAll() // 로그인 페이지는 누구나 접근 가능해야 함
		);

		// 5. 로그아웃 설정 수정
		httpSecurity.logout(logout -> logout.logoutUrl("/logout") // 로그아웃을 처리할 URL (기본값: /logout)
				.logoutSuccessUrl("/login") // 로그아웃 성공 시 이동할 페이지
				.invalidateHttpSession(true) // HTTP 세션 무효화 (기본값: true)
				.deleteCookies("JSESSIONID", "remember-me") // 로그아웃 시 관련 쿠키 삭제
				.permitAll() // 로그아웃 요청은 누구나 접근 가능해야 함
		);

		return httpSecurity.build();
	}

	//@Autowired
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(createUserDetailsService()).passwordEncoder(createPasswordEncoder());
	}

	// 스프링 시큐리티의 UserDetailsService를 구현한 클래스를 빈으로 등록한다.
	@Bean
	public UserDetailsService createUserDetailsService() {
		return new CustomUserDetailsService();
	}

	// 사용자가 정의한 비번 암호화 처리기를 빈으로 등록한다.
	@Bean
	public PasswordEncoder createPasswordEncoder() {
		return new CustomNoOpPasswordEncoder();
	}

	// 3. 접근거부시 예외처리를 설정을 클래스로 이동한다.
	@Bean
	public AccessDeniedHandler createAccessDeniedHandler() {
		return new CustomAccessDeniedHandler();

	}

	public AuthenticationSuccessHandler createAuthenticationSuccessHandler() {
		return new CustomLoginSuccessHandler();

	}
}
