package com.example.demo.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class AdminController {
	private final UserRepository _userRepository;
	
	public AdminController(UserRepository userRepository) {
		this._userRepository = userRepository;
	}
	
    @GetMapping("/admin/hello")
    public String adminHello() {
        return "管理者専用ページへようこそ！";
    }
    
    @PutMapping("/admin/{id}/role") // 更新なので PUT
    public ResponseEntity<String> updateUserRole(@PathVariable Long id) {
        // 1. まずはDBから今のユーザー情報を探す
        Optional<UserEntity> userOpt = _userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // ユーザーがいなければ 404
        }

        // 2. 取得したデータの「ロール」だけを書き換える
        UserEntity user = userOpt.get();
        user.setRole("ROLE_ADMIN");

        // 3. 保存（パスワードなどは元のまま維持される）
        _userRepository.save(user);

        return ResponseEntity.ok(user.getUsername() + " を管理者に昇格させました！");
    }
}
