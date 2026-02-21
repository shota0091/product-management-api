package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ProductEntity;

@Service // ★ポイント1: これをつける！
public class CreateOutPutCSV {
	
	// ★ポイント2: staticを外す、小文字始まりにする
	public ResponseEntity<byte[]> outputCsv(List<ProductEntity> products) {
		
		StringBuilder csv = new StringBuilder();
		csv.append("ID,商品名,価格\n");
		
		for (ProductEntity p : products) {
			csv.append(p.getId()).append(",")
			   .append(p.getName()).append(",")
			   .append(p.getPrice()).append("\n");
		}
		
		byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"products.csv\"");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
		
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}
}