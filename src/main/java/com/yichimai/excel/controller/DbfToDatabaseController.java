package com.yichimai.excel.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yichimai.excel.utils.DbfToDatabaseUtil;
import com.yichimai.excel.utils.ResponseUtil;


@RestController
@RequestMapping("/dbf")
public class DbfToDatabaseController {
	private static Logger LOGGER = LoggerFactory.getLogger(DbfToDatabaseController.class);
	
	@PostMapping("/dbfToMysql")
	public Object handleExcel(String databaseType,String mysqlUrl,String mysqlUser,String mysqlPass,
			String newTableName,String isCreateTable,String charset,MultipartFile uploadFile
			) {
		Map<String, Object> result = null;
		try {
			DbfToDatabaseUtil.handle(databaseType, mysqlUrl, mysqlUser, mysqlPass, 
					newTableName, Boolean.parseBoolean(isCreateTable), uploadFile, charset);
			result = ResponseUtil.createSuccessResponse(null);
			
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseUtil.createErrorResponse(e.getMessage());
			return result;
		}
		return result;
	}
	
	
	@PostMapping("/testMultiFile")
	public Object testMultiFile(MultipartFile uploadFile			) {
		Map<String, Object> result = null;
		try {
			
			long size = uploadFile.getSize();
			System.out.println("fileSize = " + size);
			
			result = ResponseUtil.createSuccessResponse(null);
			
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseUtil.createErrorResponse(e.getMessage());
			return result;
		}
		return result;
	}
	
	
}
