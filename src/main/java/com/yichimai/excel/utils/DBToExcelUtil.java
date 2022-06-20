package com.yichimai.excel.utils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.yichimai.dbutil.DBUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.db.DbUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

public class DBToExcelUtil {

	public static void main(String[] args) {
//		generateExcelFile("select * from zyml");
		
	}
	
	public static void generateExcelFile(OutputStream outputStream,String sql) {
		DBUtil.init("db.cfg", "mysql");
        List<Map<String, Object>> mapList = DBUtil.queryMapList(sql);//查数据库
        //获取列名
        Set<String> titleSet = mapList.get(0).keySet();
        List<List<String>> wholeExcelData = new ArrayList<>();//准备写入excel的结构体
        //写第一列表头
        List<String> titleLine = CollUtil.newArrayList(titleSet); wholeExcelData.add(titleLine);
        
        //写行
        for(Map<String, Object> item : mapList) {
        	List<String> currentLine = new ArrayList<>();
        	titleSet.forEach( (key) -> {
        		Object object = item.get(key);
        		currentLine.add(object == null?"":object.toString());
        	});
        	wholeExcelData.add(currentLine);
        }
        
        ExcelWriter writer = ExcelUtil.getBigWriter();
//        BigExcelWriter writer= ExcelUtil.getBigWriter("D:/xxx.xlsx");
        // 一次性写出内容，使用默认样式
        writer.write(wholeExcelData);
        writer.flush(outputStream, true);
        // 关闭writer，释放内存
        writer.close();
	}
	
	
}
