package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.ProductEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CreateOutPutCSV;
import com.example.demo.service.ProductService;
import com.example.demo.util.DownloadUtil;

@RestController
@RequestMapping("/product")
public class ProductController {
	
	private final ProductRepository _productRepository;
	private final CreateOutPutCSV _createOutPutCSV;
	private final UserRepository _userRepository;
	private final ProductService _productService;
	
	public ProductController(
			ProductRepository productRepository,
			UserRepository userRepository,
			CreateOutPutCSV createOutPutCSV,
			ProductService productService) {
				this._productRepository = productRepository;
				this._userRepository = userRepository;
				this._createOutPutCSV = createOutPutCSV;
				this._productService = productService;
				}
	
	@GetMapping("/test")
	public Map<String, String> hello() {
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
	@PutMapping("/editProductInfo") 
	public ResponseEntity<ProductEntity> editProduct(
			@RequestBody ProductEntity productEntity,
			Authentication authentication) {
		try {
			ProductEntity entity = _productService.updateProduct(productEntity, authentication.getName());
			return ResponseEntity.ok(entity);
		} catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // 404
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
        }
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
	public ProductEntity createProduct(
			@Validated @RequestBody 
			ProductEntity productEntity,
			Authentication authentication){
		
		// 1. ログイン中のユーザー（トークンの持ち主）の名前を取得
        String username = authentication.getName();
        
        // 2. DBからそのユーザーのEntityを取得
        UserEntity loginUser = _userRepository.findByUsername(username);
        
        // 3. 登録しようとしている商品に、「登録者」としてセットする
        productEntity.setUser(loginUser);
		
		return _productRepository.save(productEntity);
	}
	
	/**
	 * 削除
	 * @param id
	 * @return
	 */
	@DeleteMapping("/deleteProductInfo/{id}") 
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication authentication) {
	    
	    Optional<ProductEntity> productOpt = _productRepository.findById(id);
	    
	    if (productOpt.isEmpty()) {
	        return ResponseEntity.notFound().build(); // なければ404
	    }
	    
	    try {
	    	ProductEntity existingProduct = productOpt.get();
	    	String userName = existingProduct.getUser().getUsername();
	    	
	    	// 所有者チェック（.equals を使う！）
	    	if(userName.equals(authentication.getName())) {
	    		_productRepository.deleteById(id);
		        return ResponseEntity.noContent().build(); 
	    	} else {
	    		// 他人の商品は 403 Forbidden で弾く！
	    		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	    	}
	    	
	    } catch (Exception e) {
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