package com.yichimai.excel.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.yichimai.excel.utils.ExcelHandlerUtil;
import com.yichimai.excel.utils.ExcelHandlerUtil2;
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
//		    System.out.println("//不存在");  
		    file .mkdirs();    
		} else {  
//		    System.out.println("//目录存在");  
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
			
			ExcelHandlerUtil2.paging(Integer.valueOf(workbookIndex), Integer.valueOf(cellIndex), 
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
		return null;
	}
	
	
	
}
