package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final JwtUtil _jwtUtil; // 追加
	private final UserRepository _userRepository;

    public SecurityConfig(JwtUtil jwtUtil,UserRepository userRepository) { // 追加
        this._jwtUtil = jwtUtil;
        this._userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers("/h2-console/**").permitAll()
            	    .requestMatchers("/api/signup", "/api/login").permitAll()
            	    .requestMatchers("/api/admin/**").hasRole("ADMIN") 
            	    .requestMatchers("/api/product/**").hasAnyRole("USER", "ADMIN")
            	    .anyRequest().authenticated()
            	)

            	.addFilterBefore(new JwtAuthenticationFilter(_jwtUtil,_userRepository), UsernamePasswordAuthenticationFilter.class);
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 業界標準のハッシュ化アルゴリズム「BCrypt」を使用する
        return new BCryptPasswordEncoder();
    }
}