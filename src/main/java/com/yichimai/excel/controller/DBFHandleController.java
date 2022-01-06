package com.yichimai.excel.controller;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.yichimai.excel.utils.DbfFillUtil;
import com.yichimai.excel.utils.ExcelHandlerUtil1;
import com.yichimai.excel.utils.ResponseUtil;


@Controller
@RequestMapping("/dbfHandle")
public class DBFHandleController {

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
	@PostMapping("/fillField")
	public Object handleExcel(String charset,MultipartFile uploadFile,
			HttpServletResponse response) {
		checkDir();
		Map<String, Object> result = null;
		try {
			String nowStr = DTF_YMDHMS.format(ZonedDateTime.now());
			String targetFileName = "excelOutput" + nowStr + ".dbf";
			DbfFillUtil.handle(uploadFile, charset, downloadFileFolderPath + targetFileName);
			
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
}
