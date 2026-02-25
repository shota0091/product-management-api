package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "users") // "user"はDBの予約語であることが多いため"users"を推奨
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "ユーザー名は必須です")
    @Column(unique = true) // ユーザー名の重複を許さない
    private String username;

    @NotBlank(message = "パスワードは必須です")
    @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!-/:-@\\[-`{-~])[!-~]{8,}$", 
             message = "パスワードは大文字・小文字・数字・記号を含めて8文字以上で設定してください")
    @JsonIgnore
    private String password;

    // 権限（一般ユーザー：ROLE_USER, 管理者：ROLE_ADMIN など）
    private String role; 
}