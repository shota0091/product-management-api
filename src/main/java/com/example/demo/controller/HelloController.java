package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.ProductEntity;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.CreateOutPutCSV;

@RestController
public class HelloController {
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	CreateOutPutCSV createOutPutCSV;
	
	@GetMapping("/api/hello")
	public Map<String, String> hello() {
		// Mapを返すと、Springが勝手に {"message": "Hello World", "status": "success"} というJSONにしてくれます！
		return Map.of(
			"message", "Hello World",
			"status", "success"
		);
	}
	
	/**
	 * パスパラメータの受け取り
	 * @param id
	 * @return
	 */
	@GetMapping("/api/hello/{id}")
	public Map<String, Object> getPassParameterId(@PathVariable Long id) {
		return Map.of(
			"message", id,
			"status", "success"
		);
	}
	
	/**
	 * クエリパラメータの受け取り
	 * @param name
	 * @param pass
	 * @return
	 */
	@GetMapping("/api/hello/user")
	public Map<String, Object> getPassParameter(@RequestParam String name, @RequestParam String pass) { 
		return Map.of(
			"message", Map.of("UserName", name, "Password", pass),
			"status", "success"
		);
	}
	

	
	/**
	 * 商品一覧取得
	 * @return
	 */
	@GetMapping("/api/hello/getProductsInfo")
	public List<ProductEntity> getProducts(){
		return productRepository.findAll();
	}
	
	/**
	 * 主キー検索
	 * @param id
	 * @return
	 */
	@GetMapping("/api/hello/getProductInfo/{id}")
	public Optional<ProductEntity> getProduct(@PathVariable Long id){
		return productRepository.findById(id);
	}
	
	/**
	 * CSV出力
	 * @param id
	 * @return
	 */
	@GetMapping("/api/hello/outPutCSV/ProductInfoAll")
	public ResponseEntity<byte[]> outProductsCSV(){
		return createOutPutCSV.outputCsv(productRepository.findAll());
	}
	
	/**
	 * 指定のProductのCSV出力
	 * @param id
	 * @return
	 */
	@PostMapping("/api/hello/outPutCSV/ProductInfo")
	public ResponseEntity<byte[]> outProductCSV(@RequestBody List<ProductEntity> products){
		// 1. リクエストから「IDのリスト」だけを抽出する
	    List<Long> ids = new ArrayList<>();
	    for (ProductEntity p : products) {
	        ids.add(p.getId());
	    }
	    
	    // 2. 「findAllById」を使って、DBから一括で検索してリストにする！（for文で毎回検索しない）
	    List<ProductEntity> productsList = productRepository.findAllById(ids);
		return createOutPutCSV.outputCsv(productsList);
	}
	
	
	/**
	 * 変更保存
	 * @param id
	 * @return
	 */
	@PostMapping("/api/hello/editProductInfo")
	public ResponseEntity<ProductEntity> editProduct(@RequestBody ProductEntity productEntity) {
	    // IDが存在するかチェック（安全策）
	    if (productEntity.getId() == null || !productRepository.existsById(productEntity.getId())) {
	        return ResponseEntity.notFound().build(); // 404を返す
	    }
	    ProductEntity saved = productRepository.save(productEntity);
	    return ResponseEntity.ok(saved); // 200 OK と一緒に保存結果を返す
	}
	
	/**
	 * Body情報の受け取り
	 * @param entity
	 * @return
	 */
	@PostMapping("/api/hello/getProductInfo")
	public Map<String, Object> getBodyParameter(@RequestBody ProductRequest req){
		return Map.of(
				"message", Map.of("ProductName", req.getName(), "ProductPrice", req.getPrice()),
				"status", "success"
			);
	}
	
	/**
	 * 商品DB保存
	 * @param productEntity
	 * @return
	 */
	@PostMapping("/api/hello/saveProductInfo")
	public ProductEntity createProduct(@RequestBody ProductEntity productEntity){
		return productRepository.save(productEntity);
	}
	
	/**
	 * 削除
	 * @param id
	 * @return
	 */
	@DeleteMapping("/api/hello/deleteProductInfo/{id}") // Deleteを使う
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
	    
	    // 1. 存在確認
	    if (!productRepository.existsById(id)) {
	        return ResponseEntity.notFound().build(); // なければ404
	    }
	    
	    // 2. 削除実行
	    try {
	        productRepository.deleteById(id);
	        // 3. 成功時は「204 No Content（成功したけど返す中身はないよ）」または「200 OK」
	        return ResponseEntity.noContent().build(); 
	    } catch (Exception e) {
	        // 何らかの理由で失敗したら500エラー
	        return ResponseEntity.internalServerError().build();
	    }
	}
	
	
}