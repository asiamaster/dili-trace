package com.dili.trace.util;

import org.apache.commons.lang3.StringUtils;

public class MaskUserInfo {
	public static String maskIdNo(String idNo) {
		if(StringUtils.isBlank(idNo)) {
			return idNo;
		}
		idNo=StringUtils.trim(idNo);
		if (idNo.length() == 16) {
			return replace(idNo, 8, 5, "*");
		}else if(idNo.length()==18) {
			return replace(idNo, 10, 5, "*");
		}
		return idNo;
		
	}
	public static String maskAddr(String addr) {
		if(StringUtils.isBlank(addr)) {
			return addr;
		}
		addr=StringUtils.trim(addr);
		if(addr.length()>0) {
			return replace(addr, (addr.length()-1)/3,  (addr.length()-1)/2, "*");
		}
		return addr;
		
	}
	public static String maskPhone(String phone) {
		if(StringUtils.isBlank(phone)) {
			return phone;
		}
		phone=StringUtils.trim(phone);
		if(phone.length()>0) {
			return replace(phone, (phone.length()-1)/3,  (phone.length()-1)/2, "*");
		}
		return phone;
		
	}


	private static String replace(String str,int startIndex,int length,String replacement) {
		
		if(str!=null&&str.length()>startIndex) {
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<str.length();i++) {
				char ch=str.charAt(i);
				if(i>=startIndex&&i<=(startIndex+length)) {
					sb.append(replacement);
				}else {
					sb.append(ch);	
				}
			}
			
			return sb.toString();
		}
		return str;
		
	}
	

}
