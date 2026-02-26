package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.demo.entity.ProductEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private UserEntity ownerUser;
    private ProductEntity existingProduct;

    @BeforeEach
    void setUp() {
        // テスト用の「持ち主」ユーザー
        ownerUser = new UserEntity();
        ownerUser.setId(1L);
        ownerUser.setUsername("owner_user");

        // DBに元々入っている想定の商品データ
        existingProduct = new ProductEntity();
        existingProduct.setId(100L);
        existingProduct.setName("古い商品名");
        existingProduct.setPrice("1000");
        existingProduct.setUser(ownerUser);
    }

    // ==========================================
    // ▼ 更新 (updateProduct) のテスト
    // ==========================================

    @Test
    @DisplayName("【正常系】自分の商品を正しく更新できること")
    void updateProduct_Success() {
        // 準備：更新用に入力された新しいデータ
        ProductEntity inputEntity = new ProductEntity();
        inputEntity.setId(100L);
        inputEntity.setName("新しい商品名");
        inputEntity.setPrice("5000");

        // 準備：影武者の設定（検索されたらexistingProductを返し、保存されたらそのまま返す）
        when(productRepository.findById(100L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(existingProduct);

        // 実行
        ProductEntity result = productService.updateProduct(inputEntity, "owner_user");

        // 検証：名前と価格が上書きされていること、saveが1回呼ばれたこと
        assertEquals("新しい商品名", result.getName());
        assertEquals("5000", result.getPrice());
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("【異常系】存在しない商品を更新しようとすると EntityNotFoundException が飛ぶこと")
    void updateProduct_NotFound() {
        ProductEntity inputEntity = new ProductEntity();
        inputEntity.setId(999L); // 存在しないID

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // 実行＆検証
        assertThrows(EntityNotFoundException.class, () -> {
            productService.updateProduct(inputEntity, "owner_user");
        });
        
        // 保存処理は呼ばれていないはず
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("【異常系】他人の商品を更新しようとすると AccessDeniedException が飛ぶこと")
    void updateProduct_AccessDenied() {
        ProductEntity inputEntity = new ProductEntity();
        inputEntity.setId(100L);

        when(productRepository.findById(100L)).thenReturn(Optional.of(existingProduct));

        // 実行＆検証（アクセスしてきたユーザー名を別の人にする）
        assertThrows(AccessDeniedException.class, () -> {
            productService.updateProduct(inputEntity, "other_user");
        });
        
        verify(productRepository, never()).save(any());
    }


    // ==========================================
    // ▼ 削除 (deleteProduct) のテスト
    // ==========================================

    @Test
    @DisplayName("【正常系】自分の商品を正しく削除できること")
    void deleteProduct_Success() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(existingProduct));

        // 実行（エラーが出なければOK）
        assertDoesNotThrow(() -> productService.deleteProduct(100L, "owner_user"));

        // 検証：deleteが1回呼ばれたこと
        verify(productRepository, times(1)).delete(existingProduct);
    }

    @Test
    @DisplayName("【異常系】存在しない商品を削除しようとすると EntityNotFoundException が飛ぶこと")
    void deleteProduct_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            productService.deleteProduct(999L, "owner_user");
        });

        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("【異常系】他人の商品を削除しようとすると AccessDeniedException が飛ぶこと")
    void deleteProduct_AccessDenied() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(existingProduct));

        assertThrows(AccessDeniedException.class, () -> {
            productService.deleteProduct(100L, "other_user");
        });

        verify(productRepository, never()).delete(any());
    }
}