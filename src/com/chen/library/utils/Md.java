package com.chen.library.utils;

import java.security.MessageDigest;

import android.text.TextUtils;

public class Md {
	/**
	 * MD5加密
	 * 
	 * @param 需要加密的String
	 * @return 加密后String
	 */
	public final static String MD5(String s) {
		if (TextUtils.isEmpty(s)) {
			return null;
		}
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			// 使用MD5创建MessageDigest对象
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte b = md[i];
				str[k++] = hexDigits[b >> 4 & 0xf];
				str[k++] = hexDigits[b & 0xf];
			}

			return new String(str).toUpperCase();
			// return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
}
