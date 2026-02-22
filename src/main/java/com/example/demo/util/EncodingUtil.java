package com.example.demo.util;

public class EncodingUtil {
	
	private EncodingUtil() {}

	/**
	 * Excel用バイト作成クラス
	 * @param data
	 * @return
	 */
	public static byte[] addUtilBom(byte[] data) {
		
		byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
		byte[] result = new byte[bom.length + data.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(data, 0, result, bom.length, data.length);
		
		return result;
	}

}
