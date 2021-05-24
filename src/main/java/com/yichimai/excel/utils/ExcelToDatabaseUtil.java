package com.yichimai.excel.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import cn.hutool.core.lang.Console;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.sax.Excel07SaxReader;
import cn.hutool.poi.excel.sax.handler.RowHandler;

public class ExcelToDatabaseUtil {

	
	public static void main(String[] args) {
		Excel07SaxReader reader = new Excel07SaxReader(createRowHandler());
		reader.read("d:/1008/test.xlsx", 0);
		
		
//		ExcelUtil.readBySax("D:\\1008\\test.xlsx", 0, createRowHandler());
		
	}
	private static String INSERT_SQL = "";
	private static PreparedStatement PS = null;
	
	private static RowHandler createRowHandler() {
	    return new RowHandler() {
	        @Override
	        public void handle(int sheetIndex, long rowIndex, List<Object> rowlist) {
	            
	            if(rowIndex == 0) {
//	            	System.out.println(rowlist.size());
	            	try {
						createTable(rowlist.size());
						createInsertSql(rowlist.size());
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
	
	
	private static void createInsertSql(int cowSize) throws Exception {
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbf?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC", "root", "root");
		
		/**
		 * create the insert sql statement
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("insert into testExcel" );
		sb.append("(");
		for(int i = 0; i<cowSize ; i++) {
			sb.append("c" + i +  ",");
		}
		sb.setLength(sb.length()-1);//  remove the comma at the end
		sb.append(")");
		sb.append("values(");
		for(int i = 0; i<cowSize ; i++) {
			sb.append("?,");
		}
		sb.setLength(sb.length()-1);//  remove the comma at the end
		sb.append(")");
		
		INSERT_SQL = sb.toString();
		PS = conn.prepareStatement(INSERT_SQL);
		
	}
	
	
	private static void createTable(int cowSize) throws Exception {
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbf?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC", "root", "root");
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
