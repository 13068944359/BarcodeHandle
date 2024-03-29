package com.yichimai.excel.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.yichimai.excel.dbfToCheck.HandleCenter;
import com.yichimai.excel.utils.ExcelHandlerUtil1;
import com.yichimai.excel.utils.ExcelHandlerUtil2;
import com.yichimai.excel.utils.ExcelToDatabaseUtil;
import com.yichimai.excel.utils.ResponseUtil;



@Controller
@RequestMapping("/excel")
public class ExcelHandleController {

	private static final DateTimeFormatter DTF_YMDHMS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	
	@Value("${xiaojz.downloadPath}")
	private String downloadFileFolderPath;
	
	private void checkDir() {
		File file =new File(downloadFileFolderPath);    
		//如果文件夹不存在则创建    
		if  (!file .exists()  && !file .isDirectory())    {       
		    file.mkdirs();    
		} else {  
		} 
	}
	
	@ResponseBody
	@PostMapping("/handleExcel")
	public Object handleExcel(String workbookIndex,String cellIndex,MultipartFile uploadFile,
			String pageLandscape,String pageSize,HttpServletResponse response) {
		checkDir();
		Map<String, Object> result = null;
		try {
			String nowStr = DTF_YMDHMS.format(ZonedDateTime.now());
			String targetFileName = "excelOutput" + nowStr + ".xlsx";
			
			ExcelHandlerUtil1.paging(Integer.valueOf(workbookIndex), Integer.valueOf(cellIndex), 
					Boolean.valueOf(pageLandscape), Integer.valueOf(pageSize),uploadFile, downloadFileFolderPath + targetFileName);
			
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("name", targetFileName);
			result = ResponseUtil.createSuccessResponse(map);
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseUtil.createErrorResponse(e.getMessage());
			return result;
		}
		return result;
	}
	
	
	@ResponseBody
	@PostMapping("/handleExcel2")
	public Object handleExcel2(String pageTitle,String workbookIndex,String spilitSchoolCellIndex,MultipartFile uploadFile,
			String pageLandscape,String pageSize,String examType,HttpServletResponse response) {
		checkDir();
		Map<String, Object> result = null;
		try {
			String nowStr = DTF_YMDHMS.format(ZonedDateTime.now());
			String targetFileName = "excelOutput" + nowStr + ".xlsx";
			
			ExcelHandlerUtil2.paging(pageTitle,examType,Integer.valueOf(workbookIndex), spilitSchoolCellIndex, 
					Boolean.valueOf(pageLandscape), Integer.valueOf(pageSize),uploadFile, downloadFileFolderPath + targetFileName);
			
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("name", targetFileName);
			result = ResponseUtil.createSuccessResponse(map);
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseUtil.createErrorResponse(e.getMessage());
			return result;
		}
		return result;
	}
	
	
	@ResponseBody
	@PostMapping("/handleExcel4")
	public Object handleExcel4(MultipartFile uploadFile,
			String pageSize,String examType,String encodeType,HttpServletResponse response) {
		checkDir();
		Map<String, Object> result = null;
		try {
			String nowStr = DTF_YMDHMS.format(ZonedDateTime.now());
			String targetFileName = "excelOutput" + nowStr + ".xlsx";
			
			List<String> logList = HandleCenter.start(uploadFile, Integer.valueOf(pageSize), downloadFileFolderPath + targetFileName, examType, encodeType);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("name", targetFileName);
			logList.add("<span>【处理成功】文件处理完成，即将开始下载" + targetFileName + "</span>");
			map.put("log", logList);
			result = ResponseUtil.createSuccessResponse(map);
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseUtil.createErrorResponse(e.getMessage());
			return result;
		}
		return result;
	}
	
	
	@GetMapping("/downloadExcel")
	public Object downloadExcel(String fileName,HttpServletResponse response) {
		File file = new File(downloadFileFolderPath + fileName);
		if(!file.exists()) {
			return "获取文件异常！";
		}
		
		FileInputStream fileInputStream = null;// 文件输入流
		OutputStream outputStream = null;// 文件输出流那样
		try {
			outputStream = response.getOutputStream();
			response.reset();
			response.setHeader("Content-disposition", "attachment;filename=" + fileName);
			response.setContentType("application/download");
			
			byte[] byteArr = new byte[1024];
			long fileLength = file.length();
			String length1 = String.valueOf(fileLength);
			response.setHeader("Content_Length", length1);
			fileInputStream = new FileInputStream(downloadFileFolderPath + fileName);
			int n = 0;
			while ((n = fileInputStream.read(byteArr)) != -1) {
				outputStream.write(byteArr, 0, n);
			}
			outputStream.flush();
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		file.deleteOnExit();//delete after download
		
		return null;
	}
	
	
	
	
	@PostMapping("/excelToDB")
	public Object handleExcel(String databaseType,String DBUrl,String DBUser,String DBPass,
			String newTableName,String isCreateTable,MultipartFile uploadFile,Integer sheetIndex
			) {
		Map<String, Object> result = null;
		try {
			ExcelToDatabaseUtil.handle(databaseType, DBUrl, DBUser, DBPass, newTableName, Boolean.parseBoolean(isCreateTable), uploadFile, sheetIndex);
			result = ResponseUtil.createSuccessResponse(null);
			
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseUtil.createErrorResponse(e.getMessage());
			return result;
		}
		return result;
	}
	
	
	
	
	
	
}
