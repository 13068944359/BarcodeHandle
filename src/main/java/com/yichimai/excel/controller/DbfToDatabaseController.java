package com.yichimai.excel.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
		}
		return result;
	}
	
	
	@PostMapping("/dbfToDatabase")
	public Object dbfToDatabase(String databaseType,String mysqlUrl,String mysqlUser,String mysqlPass,
			String newTableName,String isCreateTable,String charset,MultipartFile[] uploadFile   //多个文件需要用数组接收
			) {
		Map<String, Object> result = null;
		try {
			DbfToDatabaseUtil.handle(databaseType, mysqlUrl, mysqlUser, mysqlPass, 
					newTableName, Boolean.parseBoolean(isCreateTable), uploadFile, charset);
			result = ResponseUtil.createSuccessResponse(null);
			
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseUtil.createErrorResponse(e.getMessage());
		}
		return result;
	}
	
	
	
	@PostMapping("/testMultiFile")
	public Object testMultiFile(@RequestParam("uploadFile") MultipartFile[] uploadFile			) {
		Map<String, Object> result = null;
		try {
			long size = uploadFile.length;
			System.out.println("fileSize = " + size);
			
			for(MultipartFile f : uploadFile) {
				
				System.out.println(f.getOriginalFilename());
			}
			
			result = ResponseUtil.createSuccessResponse(null);
			
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseUtil.createErrorResponse(e.getMessage());
		}
		return result;
	}
	
	
}
