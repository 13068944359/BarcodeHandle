package com.yichimai.excel.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResponseUtil {

//	{
//		"rtResponse": {
//			"rt_code": 200,
//			"rt_desc": "处理成功",
//			"rt_flow_id": "accessSEQ",
//			"rtResult": {
//				"count": 5
//			}
//		}
//	}
	
	public static Map<String,Object> createSuccessResponse(Map dataObject){
		Map<String,Object> responseMap = new HashMap<String, Object>();
		Map<String,Object> rtResponse = new HashMap<String, Object>();
		
		responseMap.put("rtResponse", rtResponse);
		
		rtResponse.put("rt_code", 200);
		rtResponse.put("rt_desc", "处理成功");
		rtResponse.put("rt_flow_id", UUID.randomUUID());
		rtResponse.put("rtResult", dataObject);
		
		return responseMap;
	}
	
	
	public static Map<String,Object> createErrorResponse(String errorMsg){
		Map<String,Object> responseMap = new HashMap<String, Object>();
		Map<String,Object> rtResponse = new HashMap<String, Object>();
		
		responseMap.put("rtResponse", rtResponse);
		rtResponse.put("rt_code", 500);
		rtResponse.put("rt_desc", errorMsg);
		rtResponse.put("rt_flow_id", UUID.randomUUID());
		return responseMap;
	}
	
	public static Map<String,Object> createNoAuthResponse(){
		Map<String,Object> responseMap = new HashMap<String, Object>();
		Map<String,Object> rtResponse = new HashMap<String, Object>();
		responseMap.put("rtResponse", rtResponse);
		rtResponse.put("rt_code", 403);
		rtResponse.put("rt_desc", "没有权限");
		rtResponse.put("rt_flow_id", UUID.randomUUID());
		return responseMap;
	}
	
}
