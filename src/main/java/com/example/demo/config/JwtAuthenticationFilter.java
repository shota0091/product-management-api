package com.example.demo.config;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil _jwtUtil;
	private final UserRepository _userRepository;
	
	public JwtAuthenticationFilter(JwtUtil jwtUtil,UserRepository userRepository) {
		this._jwtUtil = jwtUtil;
		this._userRepository = userRepository;
	}
	
	
	@Override
    protected void doFilterInternal(HttpServletRequest req, 
    		HttpServletResponse res, 
    		FilterChain filterChain) 
    				throws ServletException, IOException{
		
		String authHeader = req.getHeader("Authorization");
		
		if(authHeader != null && authHeader.startsWith("Bearer")) {
			String token = authHeader.substring(7);
			String username;
			try {
			    // 既存のトークン解析処理...
			    username = _jwtUtil.getUsernameFromToken(token);
			    // ...
			} catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.ExpiredJwtException e) {
			    // 1. レスポンスの形式を「JSON」に指定
				res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
				res.setContentType("application/json; charset=UTF-8");

			    // 2. 返したいエラーメッセージを組み立てる
			    Map<String, String> errorMap = new HashMap<>();
			    errorMap.put("error", "Invalid or expired token");
			    errorMap.put("message", "トークンが無効、または期限切れです。再度ログインしてください。");

			    // 3. JSON文字列に変換して書き出す
			    String json = new ObjectMapper().writeValueAsString(errorMap);
			    res.getWriter().write(json);
			    
			    return; // 【重要】ここで処理を終了させ、Controllerへは行かせない！
			}
			
			if (_jwtUtil.validateToken(token, username)) {
				UserEntity user = _userRepository.findByUsername(username);
				List<SimpleGrantedAuthority> authorities = 
				        Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));

				    // 2. 認証オブジェクトを作成
				    UsernamePasswordAuthenticationToken authentication = 
				        new UsernamePasswordAuthenticationToken(username, null, authorities);

				    // 【重要！】ここが if の中に入っている必要があります
				    SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		filterChain.doFilter(req, res);
		
	}
}
