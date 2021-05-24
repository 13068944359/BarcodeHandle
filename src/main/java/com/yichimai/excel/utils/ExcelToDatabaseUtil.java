package com.yichimai.excel.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.lang.Console;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.sax.Excel07SaxReader;
import cn.hutool.poi.excel.sax.handler.RowHandler;

/**
 * 把excel文件导入数据库
 *
 */
public class ExcelToDatabaseUtil {

	
	public static void main(String[] args) {
//		Excel07SaxReader reader = new Excel07SaxReader(createRowHandler());
//		reader.read("d:/1008/test.xlsx", 0);
		
		
	}
	
	private static PreparedStatement PS = null;
	
	private static RowHandler createRowHandler() {
	    return new RowHandler() {
	        @Override
	        public void handle(int sheetIndex, long rowIndex, List<Object> rowlist) {
	            
	            if(rowIndex == 0) {
//	            	System.out.println(rowlist.size());
	            	try {
	            		createTableAndInsertSql(rowlist);
//						createInsertSql(rowlist.size());
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }else {
	    			try {
		            	for(int index=0; index<rowlist.size(); index++	) {
		    				PS.setString(index + 1, rowlist.get(index).toString().trim());
		    			}
						PS.execute();
						System.out.println("executed  ~ " + rowIndex);
					} catch (SQLException e) {
						e.printStackTrace();
					}
	            	
	            }
	            
	        }
	    };
	}
	
	private static final String URL = "jdbc:mysql://localhost:3306/dbf?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
	private static final String USERNAME = "root";
	private static final String PASS = "root";
	
	
	private static void createTableAndInsertSql(List<Object> rowlist) throws Exception {
		
		Connection conn = DriverManager.getConnection(URL, USERNAME, PASS);
		Statement stat = conn.createStatement();
		
		StringBuilder createTableSB = new StringBuilder();
		createTableSB.append("create table testExcel");
		createTableSB.append("(");
		createTableSB.append("id int primary key AUTO_INCREMENT");  // first title must be id, auto increment

		OLD_COLUMN_NAME_COUNT_MAP = new HashMap<>();//重置一下
		StringBuilder insertSB = new StringBuilder();
		insertSB.append("insert into testExcel(");
		for(int i = 0; i<rowlist.size() ; i++) {
			
			String currentCellString = rowlist.get(i).toString().trim();
			String py = PinyinUtil.getFirstLetter(currentCellString, "");//获取拼音首字母，作为字段候选
			
			//确定了字段拼写方式后，需要对字段进行去重操作
			py = unrepeatColumnName(py);
			
			createTableSB.append(", " + py + " varchar(400)");//建表语句加字段
			
			insertSB.append( py +  ",");//insert语句加字段
		}
		insertSB.setLength(insertSB.length()-1);//  remove the comma at the end
		insertSB.append(") values(");
		for(int i = 0; i<rowlist.size() ; i++) {
			insertSB.append("?,");
		}
		insertSB.setLength(insertSB.length()-1);//  remove the comma at the end
		insertSB.append(")");
		PS = conn.prepareStatement(insertSB.toString());
		
		createTableSB.append(")");
		
		stat.executeUpdate(createTableSB.toString());
		stat.close();
		conn.close();
	}
	
	private static Map<String,Integer> OLD_COLUMN_NAME_COUNT_MAP = new HashMap<>();
	//字段名称去重（后面加上数字的方式）
	private static String unrepeatColumnName(String originName) {
		if(OLD_COLUMN_NAME_COUNT_MAP.containsKey(originName)) {
			Integer count = OLD_COLUMN_NAME_COUNT_MAP.get(originName);
			count++;
			OLD_COLUMN_NAME_COUNT_MAP.put(originName, count);
			return originName + count;
		}else {
			OLD_COLUMN_NAME_COUNT_MAP.put(originName, 1);
			return originName;
		}
	}
	
	
//	private static void createInsertSql(int cowSize) throws Exception {
//		Connection conn = DriverManager.getConnection(URL, USERNAME, PASS);
//		/**
//		 * create the insert sql statement
//		 */
//		StringBuilder sb = new StringBuilder();
//		sb.append("insert into testExcel" );
//		sb.append("(");
//		for(int i = 0; i<cowSize ; i++) {
//			sb.append("c" + i +  ",");
//		}
//		sb.setLength(sb.length()-1);//  remove the comma at the end
//		sb.append(")");
//		sb.append("values(");
//		for(int i = 0; i<cowSize ; i++) {
//			sb.append("?,");
//		}
//		sb.setLength(sb.length()-1);//  remove the comma at the end
//		sb.append(")");
//		
//		INSERT_SQL = sb.toString();
//		PS = conn.prepareStatement(INSERT_SQL);
//		
//	}
	
	
	
	/**
	 * 废弃的方法
	 * @param cowSize
	 * @throws Exception
	 */
	private static void createTable2(int cowSize) throws Exception {
		
		Connection conn = DriverManager.getConnection(URL, USERNAME, PASS);
		Statement stat = conn.createStatement();
		
		StringBuilder sb = new StringBuilder();
		sb.append("create table testExcel");
		sb.append("(");
		sb.append("id int primary key AUTO_INCREMENT");  // first title must be id, auto increment

		for(int i = 0; i<cowSize ; i++) {
			sb.append(", c" + i + " varchar(400)");
		}
		
		sb.append(")");
		
		stat.executeUpdate(sb.toString());
		stat.close();
		conn.close();
	}
	
}
