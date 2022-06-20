package com.yichimai.excel.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yichimai.excel.utils.DBToExcelUtil;

@Controller
@RequestMapping("/DBToExcel")
public class DBToExcelController {
	private static final DateTimeFormatter DTF_YMDHMS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	
	
	
	@PostMapping("/sqlToExcel")
	public Object handleExcel(String sql, HttpServletResponse response) {
//		System.out.println(sql);
		sql = sql.replaceAll("(\\r\\n|\\n|\\\\n|\\s)", " ");//换行符替换成空格，多个空格也替换成一个
//		System.out.println(sql);
		
		Map<String, Object> result = null;
		String nowStr = DTF_YMDHMS.format(ZonedDateTime.now());
		String targetFileName = "excel" + nowStr + ".xlsx";
		
		OutputStream outputStream = null;// 文件输出流那样
		try {
			outputStream = response.getOutputStream();
			response.reset();
			response.setHeader("Content-disposition", "attachment;filename=" + targetFileName);
			response.setContentType("application/download");
			
			DBToExcelUtil.generateExcelFile(outputStream, sql);
			outputStream.flush();
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
