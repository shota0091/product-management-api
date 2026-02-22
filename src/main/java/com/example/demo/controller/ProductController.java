package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.ProductEntity;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.CreateOutPutCSV;
import com.example.demo.util.DownloadUtil;

@RestController
@RequestMapping("/product")
public class ProductController {
	
	private final ProductRepository _productRepository;
	private final CreateOutPutCSV _createOutPutCSV;
	
	public ProductController(ProductRepository productRepository,
			CreateOutPutCSV createOutPutCSV) {
				this._productRepository = productRepository;
				this._createOutPutCSV = createOutPutCSV;
		
	}
	
	@GetMapping("/test")
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
	@GetMapping("/test/{id}")
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
	@GetMapping("/accsess/user")
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
	@GetMapping("/getProductsInfo")
	public List<ProductEntity> getProducts(){
		return _productRepository.findAll();
	}
	
	/**
	 * 主キー検索
	 * @param id
	 * @return
	 */
	@GetMapping("/getProductInfo/{id}")
	public Optional<ProductEntity> getProduct(@PathVariable Long id){
		return _productRepository.findById(id);
	}
	
	/**
	 * CSV出力
	 * @param id
	 * @return
	 */
	@GetMapping("/outPutCSV/ProductInfoAll")
	public ResponseEntity<byte[]> outProductsCSV(){
		return outputCSV(_productRepository.findAll());
	}
	
	/**
	 * あいまい検索
	 * @param keyword
	 * @return
	 */
	@GetMapping("/Like")
	public ResponseEntity<List<ProductEntity>> getKeywordSerch(@RequestParam("keyword") String keyword){
		List<ProductEntity> serch = _productRepository.findByNameContaining(keyword);
		return ResponseEntity.ok(serch);
	}
	
	/**
	 * 指定のProductのCSV出力
	 * @param id
	 * @return
	 */
	@PostMapping("/outPutCSV/ProductInfo")
	public ResponseEntity<byte[]> outProductCSV(@RequestBody List<ProductEntity> products){
	    List<Long> ids = new ArrayList<>();
	    for (ProductEntity p : products) {
	        ids.add(p.getId());
	    }
	    
	    List<ProductEntity> productsList = _productRepository.findAllById(ids);
		return outputCSV(productsList);
	}
	
	
	/**
	 * 変更保存
	 * @param id
	 * @return
	 */
	@PostMapping("/editProductInfo")
	public ResponseEntity<ProductEntity> editProduct(@RequestBody ProductEntity productEntity) {
	    // IDが存在するかチェック（安全策）
	    if (productEntity.getId() == null || !_productRepository.existsById(productEntity.getId())) {
	        return ResponseEntity.notFound().build(); // 404を返す
	    }
	    ProductEntity saved = _productRepository.save(productEntity);
	    return ResponseEntity.ok(saved); // 200 OK と一緒に保存結果を返す
	}
	
	/**
	 * Body情報の受け取り
	 * @param entity
	 * @return
	 */
	@PostMapping("/getProductInfo")
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
	@PostMapping("/insertProduct")
	public ProductEntity createProduct(@Validated @RequestBody ProductEntity productEntity){
		return _productRepository.save(productEntity);
	}
	
	/**
	 * 削除
	 * @param id
	 * @return
	 */
	@DeleteMapping("/deleteProductInfo/{id}") // Deleteを使う
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
	    
	    if (!_productRepository.existsById(id)) {
	        return ResponseEntity.notFound().build(); // なければ404
	    }
	    
	    try {
	    	_productRepository.deleteById(id);
	        return ResponseEntity.noContent().build(); 
	    } catch (Exception e) {
	        // 何らかの理由で失敗したら500エラー
	        return ResponseEntity.internalServerError().build();
	    }
	}
	
	/**
	 * CSV出力メソッド
	 * @param products
	 * @return
	 */
	private ResponseEntity<byte[]> outputCSV(List<ProductEntity> products) {
		byte[] csvByte = _createOutPutCSV.outputProductCsv(products);
		return DownloadUtil.fileDownloadResponse(csvByte, "ProductCSV.csv", "text/csv; charset=UTF-8");
	}
	
}