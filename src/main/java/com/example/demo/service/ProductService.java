package com.example.demo.service;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.ProductEntity;
import com.example.demo.repository.ProductRepository;

@Service // これをつけることでSpringの管理下に入ります
public class ProductService {

    private final ProductRepository _productRepository;

    public ProductService(ProductRepository productRepository) {
        this._productRepository = productRepository;
    }

    /**
     * 商品の更新ロジック
     */
    @Transactional // 途中でエラーが起きたらDBの変更を自動で取り消してくれる魔法のアノテーション
    public ProductEntity updateProduct(ProductEntity inputEntity, String currentUsername) {
        
        // 1. DBから探す
        ProductEntity existingProduct = _productRepository.findById(inputEntity.getId())
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません"));
        
        // 2. 所有者チェック
        if (!existingProduct.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("この商品を編集する権限がありません");
        }
        
        // 3. データの書き換え
        existingProduct.setName(inputEntity.getName());
        existingProduct.setPrice(inputEntity.getPrice());
        
        // 4. 保存して返す
        return _productRepository.save(existingProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id, String currentUsername) {
        
        // 1. DBから探す（なければ EntityNotFoundException を投げる）
        ProductEntity existingProduct = _productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません"));
        
        // 2. 所有者チェック（商品の名前ではなく、紐づくユーザー名と比較！）
        if (!existingProduct.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("この商品を削除する権限がありません");
        }
        
        // 3. 削除を実行
        _productRepository.delete(existingProduct);
    }
}