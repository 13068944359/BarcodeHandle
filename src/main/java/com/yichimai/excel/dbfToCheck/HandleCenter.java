package com.yichimai.excel.dbfToCheck;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.yichimai.excel.dbfToCheck.handler.HandleJszg;
import com.yichimai.excel.dbfToCheck.handler.HandleWnygz;
import com.yichimai.excel.dbfToCheck.handler.HandleZk;
import com.yichimai.excel.dbfToCheck.handler.HandleZz;
import com.yichimai.excel.dbfToCheck.handler.HandlerCrgk;
import com.yichimai.excel.dbfToCheck.handler.HandlerGatlz;
import com.yichimai.excel.dbfToCheck.handler.HandlerGk;
import com.yichimai.excel.dbfToCheck.handler.HandlerSj;
import com.yichimai.excel.dbfToCheck.handler.HandlerSk;
import com.yichimai.excel.dbfToCheck.handler.HandlerSsyjs;
import com.yichimai.excel.dbfToCheck.handler.HandlerXysp;
import com.yichimai.excel.dbfToCheck.handler.HandlerZsb;

public class HandleCenter {

	private static DBFReader checkFileType(MultipartFile uploadFile) throws Exception {
		String fileName = uploadFile.getOriginalFilename();// 获取文件名
	    String suffixName = fileName.substring(fileName.lastIndexOf("."));// 获取后缀
		
	    if(".dbf".equals(suffixName) || ".DBF".equals(suffixName)){
			InputStream fis = uploadFile.getInputStream();
			DBFReader reader = new DBFReader(fis);  //此处本可以指定读取编码，详情参照com/yichimai/excel/utils/DbfToDatabaseUtil.java
			return reader;
        }else{
            throw new Exception("file type error");
        }
	}
	
	public static List<String> start(MultipartFile uploadFile,int pageSize,String targetFilePath,String examType, String encodeType) throws Exception {
		DBFReader reader = checkFileType(uploadFile);
		List<String> logList = new LinkedList<String>();//用来传输日志信息给前台
		switch(examType) {
		case "sj":
			HandlerSj sj = new HandlerSj();
			sj.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		case "xysp":
			HandlerXysp xysp = new HandlerXysp();
			xysp.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		case "zk":
			HandleZk zk = new HandleZk();
			zk.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		case "zz":
			HandleZz zz = new HandleZz();
			zz.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		case "jszg":
			HandleJszg jz = new HandleJszg();
			jz.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		case "wnygz":
			HandleWnygz wn = new HandleWnygz();
			wn.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		case "zsb":
			HandlerZsb zsb = new HandlerZsb();
			zsb.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		case "gatlz":
			HandlerGatlz gatlz = new HandlerGatlz();
			gatlz.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		case "ptgk":
			HandlerGk ptgk = new HandlerGk();
			ptgk.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
			
		case "gksk":
			HandlerSk gksk = new HandlerSk();
			gksk.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
			
		case "ssyjs":
			HandlerSsyjs ssyjs = new HandlerSsyjs();
			ssyjs.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;

		case "crgk":
			HandlerCrgk crgk = new HandlerCrgk();
			crgk.handle(reader, pageSize, targetFilePath, encodeType, logList);
			break;
		default:
            throw new Exception("exam type error = " + examType);
		}
		
		return logList;
	}
	
	
	public static void checkTableTitle(DBFReader reader) {
		int fieldsCount = reader.getFieldCount();
		System.out.println("字段数:"+fieldsCount);
		//取出字段信息
		for( int i=0; i<fieldsCount; i++){
			DBFField field = reader.getField(i);
			System.out.println(i + "--" +field.getName());
		}
	}
	
}
