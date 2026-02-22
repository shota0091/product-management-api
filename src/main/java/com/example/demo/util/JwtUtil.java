package com.example.demo.util;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // 【重要】署名用の秘密鍵。本来は設定ファイルから読み込むべきですが、まずはここで生成します。
    // HS256アルゴリズムを使用するため、十分な長さ（256ビット以上）の鍵が必要です。
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 有効期限（例：10時間）
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    /**
     * 1. トークンの生成
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 誰のトークンか
                .setIssuedAt(new Date(System.currentTimeMillis())) // 発行時間
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 有効期限
                .signWith(SECRET_KEY) // 秘密鍵で署名（改ざん防止）
                .compact();
    }

    /**
     * 2. トークンからユーザー名を取り出す
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 3. トークンの有効性チェック
     */
    public boolean validateToken(String token, String username) {
        final String usernameInToken = getUsernameFromToken(token);
        // トークン内の名前が一致し、かつ期限切れでないこと
        return (usernameInToken.equals(username) && !isTokenExpired(token));
    }

    // --- 以下、内部で使う補助メソッド ---

    private boolean isTokenExpired(String token) {
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}