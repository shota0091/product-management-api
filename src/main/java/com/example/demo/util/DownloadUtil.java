package com.example.demo.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DownloadUtil {
	
	private DownloadUtil() {}
	
	/**
	 * ファイルダウンロードヘッダー作成クラス
	 * @param data
	 * @param fileName
	 * @param context
	 * @return
	 */
	public static ResponseEntity<byte[]> fileDownloadResponse(byte[] data,String fileName ,String context){
		
		if(data == null || data.length == 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		// headerの作成
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+ fileName + "\"");
		headers.add(HttpHeaders.CONTENT_TYPE, context);
		
		return new ResponseEntity<>(data, headers, HttpStatus.OK);
	}

}
