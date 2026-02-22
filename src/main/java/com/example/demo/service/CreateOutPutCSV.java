package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.ProductEntity;
import com.example.demo.util.EncodingUtil;

@Service
public class CreateOutPutCSV {
	

	/**
	 * ProductのCSV作成クラス
	 * @param products
	 * @return
	 */
	public byte[] outputProductCsv(List<ProductEntity> products) {
		
		StringBuilder csv = new StringBuilder();
		csv.append("ID,商品名,価格\n");
		
		for (ProductEntity p : products) {
			csv.append(p.getId()).append(",")
			   .append(p.getName()).append(",")
			   .append(p.getPrice()).append("\n");
		}
		byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
		
		return EncodingUtil.addUtilBom(csvBytes);
	}
}