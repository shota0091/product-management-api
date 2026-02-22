package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.EditPasswordRequest;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;

@RestController
@RequestMapping("/api")
public class AuthController {
	
	private final UserRepository _userRepository;
	private final PasswordEncoder _passwordEncoder;
    private final JwtUtil _jwtUtil;
    
    public AuthController(UserRepository userRepository,
    		PasswordEncoder passwordEncoder,
    		JwtUtil jwtUtil) {
    	this._userRepository = userRepository;
    	this._passwordEncoder = passwordEncoder;
    	this._jwtUtil = jwtUtil;
    }
    
    @PostMapping("/signup")
    public ResponseEntity<String> singup(@RequestBody AuthRequest req){
    	
    	UserEntity user = new UserEntity();
    	user.setUsername(req.getUsername());
    	user.setPassword(_passwordEncoder.encode(req.getPassword()));
    	user.setRole("ROLE_USER");
    	
    	_userRepository.save(user);
    	return ResponseEntity.ok("ユーザ登録を完了しました");
    }
    
    @GetMapping("/UserInfo") // 取得なのでGET
    public ResponseEntity<UserEntity> getCurrentUserInfo(Authentication authentication) {
        
        // 1. 門番（Filter）がセットしてくれた名前を取り出す
        // authentication.getName() には、JWTトークンに入っていた username が自動で入っています！
        String username = authentication.getName();

        // 2. その名前を使ってDBからユーザー情報を引っ張ってくる
        // （※ UserRepository に findByUsername メソッドがある前提です）
        UserEntity user = _userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. パスワードなどの機密情報は、念のため空にしてから返すのが安全！
        user.setPassword(""); 

        // 4. ユーザー情報をそのまま返す
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/editPassword") 
    public ResponseEntity<String> editCurrentUserPass(Authentication authentication,@RequestBody EditPasswordRequest req) {
        
        String username = authentication.getName();

        UserEntity user = _userRepository.findByUsername(username);

        if (user == null ) {
            return ResponseEntity.notFound().build();
        }
        
        if (!_passwordEncoder.matches(req.getOldPass(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("現在のパスワードが間違っています。");
        }

        user.setPassword(_passwordEncoder.encode(req.getNewPass()));
        _userRepository.save(user);

        return ResponseEntity.ok(user.getUsername() + "さんのパスワードを変更しました");
    }
    
    
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest req){
    	UserEntity user =  _userRepository.findByUsername(req.getUsername());
    	
    	if(user == null || !_passwordEncoder.matches(req.getPassword(),user.getPassword())) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "IDかパスワードが違います"));
    	}
    	
    	String token = _jwtUtil.generateToken(user.getUsername());
    	Map<String,String> map = new HashMap<>();
    	map.put("token", token);
    	return ResponseEntity.ok(map);
    }
    
    
    
}
