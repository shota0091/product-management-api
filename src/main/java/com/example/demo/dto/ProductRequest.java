package com.example.demo.dto;

import lombok.Data;

@Data
public class ProductRequest {
	
	/**
	 * 商品名
	 */
	private String name;
	
	
	/**
	 * 価格
	 */
	private String price;

}
