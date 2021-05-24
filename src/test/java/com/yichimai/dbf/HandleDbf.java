package com.yichimai.dbf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yichimai.dbutil.DBUtil;
import com.yichimai.regex.HumpAndUnderlineUtil;

public class HandleDbf {

	
	
	public static void main(String[] args) throws Exception {
		DBUtil.init("db.cfg", "mysql");
		
		
		List<Map<String, Object>> readTableData = readTableData();
		Map<String, List<String>> readMysqlSqlFile = readMysqlSqlFile();
		
		
//		for(Map<String, Object> m : readTableData) {
//			String tableName = m.get("imp_code_2").toString();
//			System.out.println("表名 " + tableName);
//			
//			List<String> list = readMysqlSqlFile.get(tableName);
//			if(list == null) {
//				System.out.println("表为空警告！！！！ " + tableName);
//			}else {
//				for(String s : list) {
//					System.out.println(s);
//				}
//			}
//			
//			
//		}
	}
	
	
	
	
	private static List<Map<String, Object>> readTableData() throws Exception{

		String sql = "SELECT * FROM 20210519_test";
		
		List<Map<String, Object>> resultList = DBUtil.queryMapList(sql, null);
		
		for(Map m: resultList){
//			System.out.println(m);
			String code = m.get("imp_code").toString();
			m.put("imp_code_2", "etype_" + HumpAndUnderlineUtil.humpToUnderline(code));
		}
		
		return resultList;
	}
	
	
	private static Map<String,List<String>> readMysqlSqlFile() throws Exception{
		Map<String,List<String>> resultMap = new HashMap<String, List<String>>();
		
		String fin = "D:\\tmp\\dbfDataHandle\\eesc.sql";
		FileInputStream fis = new FileInputStream(fin);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String currentTable = null;
		int tableCount = 0;
		
		String line = null;
		while ((line = br.readLine()) != null) {
//			System.out.println(line);
			if(line.contains("CREATE TABLE")) {   //有 CREATE TABLE 字样  （下一行开始就是字段
				String[] split = line.split("`");
				tableCount ++ ;
				currentTable = split[1];
				
				List<String> fieldList = new LinkedList<String>();
				resultMap.put(currentTable, fieldList);
				System.out.println("table name ~~ "+ currentTable);
			}else if(line.contains("PRIMARY KEY")) { //有PRIMARY KEY 字样  （字段行到此结束
				currentTable = null;
			}else {
				if(currentTable != null && !line.contains("AUTO_INCREMENT")) {
					String[] split = line.split("`");
					String fieldName = split[1];
					
					split = line.split(" ");
					String typeName = split[2];
					
					split = line.split("'");
					String comment = split[1];
					
//					System.out.println(currentTable + "\t" + fieldName + "\t" + typeName + "\t" + comment);
					resultMap.get(currentTable).add(fieldName + "\t" + typeName + "\t" + comment);
					System.out.println(fieldName + "\t" + typeName + "\t" + comment);
				}
			}
		}
	 
//		System.out.println("tableCount=" + tableCount);
		br.close();
		fis.close();
		return resultMap;
	}
	
	
	
}
