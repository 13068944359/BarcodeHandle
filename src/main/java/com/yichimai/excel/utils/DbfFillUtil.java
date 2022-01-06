package com.yichimai.excel.utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFWriter;

/**
 * 给校对中间表填充info字段
 *
 */
public class DbfFillUtil {

	private static List<String> fieldList = new ArrayList<String>();
	private static Integer info1Index ;
	
	public static void handle(MultipartFile uploadFile,String charset, String filePath) throws Exception {
		DBFReader reader = checkFileType(uploadFile, charset);
		int fieldsCount = reader.getFieldCount();//get field msg
		
		for( int i=0; i<fieldsCount; i++){
			DBFField field = reader.getField(i);
			fieldList.add(field.getName());
			
			if("info1" .equals(field.getName())) {
				info1Index = i;//  find index of info1
			}
		}
		
		FileOutputStream fos = new FileOutputStream(filePath);
        DBFWriter writer = new DBFWriter(fos,Charset.forName(charset));
        writer.setFields( createFields());
        String[] infoCache = new  String[4];
        Object[] rowFieldCache = new Object[4];
        
        Object[] rowValues;
		while((rowValues = reader.nextRecord()) != null){
			for(int i=0; i<4; i++) {
				String currentInfo = getValue(rowValues[info1Index + i]);
				
				if( !"".equals(currentInfo)  ) {
					infoCache[i] = currentInfo; // reflash cache value
					rowFieldCache[i] = getValueFromColumn(getValue(rowValues[info1Index + i]));
				}else {
					// when empty, write cache if cache not null
					if( infoCache[i] != null && !"".equals(getValue(rowValues[0])) ) {
						rowValues[info1Index + i] = rowFieldCache[i];
					}
				}
			}
			writer.addRecord(rowValues);
		}
		writer.close();
		fos.close();
	}
	
	
	private static String getValueFromColumn(String str) {
		String regex = "(\\d+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		if(m.find()) {
			String group = m.group();
			return group;
		}else {
			return "";
		}
	}
	
	
	private static DBFField[] createFields() {
		List<DBFField> outputFieldList = new ArrayList<>();
		for(String fieldName : fieldList) {
			DBFField currentF = new DBFField();
			currentF.setName( fieldName);
			currentF.setType(DBFDataType.CHARACTER);
			currentF.setLength( 50);
			
			outputFieldList.add(currentF);
		}
		
		DBFField[] array = outputFieldList.toArray(new DBFField[outputFieldList.size()]);
		return array;
	}
	
	
	private static String getValue(Object o) throws Exception {
		return o.toString ().trim();
	}
	
	
	private static DBFReader checkFileType(MultipartFile uploadFile,String charset) throws Exception {
		String fileName = uploadFile.getOriginalFilename();// 获取文件名
	    String suffixName = fileName.substring(fileName.lastIndexOf("."));// 获取后缀
		
	    if(".dbf".equals(suffixName) || ".DBF".equals(suffixName)){
			InputStream fis = uploadFile.getInputStream();
			DBFReader reader = new DBFReader(fis,Charset.forName(charset));  //此处本可以指定读取编码，详情参照com/yichimai/excel/utils/DbfToDatabaseUtil.java
			return reader;
        }else{
            throw new Exception("file type error");
        }
	}
	
}
