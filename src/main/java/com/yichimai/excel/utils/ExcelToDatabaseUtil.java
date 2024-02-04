package com.yichimai.excel.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Row;
import org.springframework.web.multipart.MultipartFile;

import com.github.binarywang.java.emoji.EmojiConverter;

import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.sax.Excel07SaxReader;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import cn.hutool.poi.exceptions.POIException;

/**
 * 把excel文件导入数据库
 */
public class ExcelToDatabaseUtil {

	
	public static void main(String[] args) {
		System.out.println("🙃Yan Rong🐳");
		System.out.println(EMOJI_CONVERTER.toHtml("🙃Yan Rong🐳"));
	}
	
	
	public static void handle(String databaseType,String DBUrl,String DBUser,String DBPass,
			String newTableName,Boolean isCreateTable,MultipartFile file,int sheetIndex) throws Exception {
		Excel07SaxReader reader = new Excel07SaxReader(createRowHandler(databaseType,DBUrl,DBUser,DBPass,newTableName,isCreateTable));
		reader.read(file.getInputStream(), sheetIndex);//sheetIndex sheet页的下标
//		test(DBUrl, DBUser, DBPass, file);//寻找无法转换的表情符
	}
	
	private static void test(String DBUrl,String DBUser,String DBPass,MultipartFile file) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");//加载数据库驱动
		SQL_CONN = DriverManager.getConnection(DBUrl, DBUser, DBPass);
		Statement stat = SQL_CONN.createStatement();
		ExcelReader testReader;
		//通过sheet编号获取
		testReader = ExcelUtil.getReader(file.getInputStream(), 0);
		int rowCount = testReader.getRowCount();
		String insertSQL = "insert into kft(n) values(?)";
		
		for(int index = 0; index < rowCount; index ++) {
			Row row = testReader.getOrCreateRow(index);
			String name = row.getCell(3).toString().trim();
			String nameAfterHandle = EMOJI_CONVERTER.toAlias(name);
			
			System.out.println(name + "====" + nameAfterHandle);

			PS = SQL_CONN.prepareStatement(insertSQL);
			PS.setString(1, nameAfterHandle);//过滤掉emoji
			
			PS.execute();
		}
		
	}
	
	
	
	private static Connection SQL_CONN = null;
	private static PreparedStatement PS = null;
	private static final EmojiConverter EMOJI_CONVERTER = EmojiConverter.getInstance();
	
	
	private static RowHandler createRowHandler(String databaseType,String DBUrl,String DBUser,String DBPass,
			String newTableName,Boolean isCreateTable) {
//		SQL_CONN = DriverManager.getConnection(DBUrl, DBUser, DBPass);
//		PS = SQL_CONN.prepareStatement(INSERT_SQL);
		
	    return new RowHandler() {
	        @Override
	        public void handle(int sheetIndex, long rowIndex, List<Object> rowlist) {
            	try {
		            if(rowIndex == 0) {
	            		createTableAndInsertSql(rowlist,databaseType,DBUrl,DBUser,DBPass,newTableName,isCreateTable);
						
		            }else {
		            	for(int index=0; index<rowlist.size(); index++	) {
//		            		String currentLine = rowlist.get(index).toString().trim();
//		            		String currentLineAfterHandle = EMOJI_CONVERTER.toAlias(rowlist.get(index).toString().trim());
		            		
	            			PS.setString(index + 1, EMOJI_CONVERTER.toAlias(rowlist.get(index).toString().trim()));//过滤掉emoji
//		            		PS.setString(index + 1, filterUtf8Mb4(rowlist.get(index).toString().trim()));//过滤掉emoji
		    			}
						PS.addBatch(); // one record in batch

	    				if(rowIndex % 10 ==0) {
							PS.executeBatch();// insert data in batch
							SQL_CONN.commit();
	    				}
		            }
            	} catch (Exception e) {
					e.printStackTrace();
					
				}
	        }
	        
	        @Override
	        public void doAfterAllAnalysed() {
	        	RowHandler.super.doAfterAllAnalysed();
				try {
					PS.executeBatch();// insert data in batch
					SQL_CONN.commit();
					
					PS.close();
					SQL_CONN.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	        }
	        
	    };
	}
	
	
	private static void createTableAndInsertSql(List<Object> rowlist,String databaseType,String DBUrl,String DBUser,String DBPass,
			String newTableName,Boolean isCreateTable) throws Exception {
		
		switch (databaseType) {
			case Const.DATABASE_TYPE_MYSQL:
				Class.forName("com.mysql.jdbc.Driver");//加载数据库驱动
				break;
	
			case Const.DATABASE_TYPE_ORACLE:
				Class.forName("oracle.jdbc.OracleDriver");//加载数据库驱动
				break;
			default:
				break;
		}
		
		
		SQL_CONN = DriverManager.getConnection(DBUrl, DBUser, DBPass);
		Statement stat = SQL_CONN.createStatement();
		SQL_CONN.setAutoCommit(false);//multi insert, multi commit
		
		StringBuilder createTableSB = new StringBuilder();
		createTableSB.append("create table " + newTableName);
		createTableSB.append("(");
		createTableSB.append("id int primary key AUTO_INCREMENT");  // first title must be id, auto increment

		OLD_COLUMN_NAME_COUNT_MAP = new HashMap<>();//重置一下
		StringBuilder insertSB = new StringBuilder();
		insertSB.append("insert into ");
		insertSB.append(newTableName);
		insertSB.append("(");
		
		for(int i = 0; i<rowlist.size() ; i++) {
			
			String currentCellString = rowlist.get(i).toString().trim();
			String py = PinyinUtil.getFirstLetter(currentCellString, "");//获取拼音首字母，作为字段候选
			
			//确定了字段拼写方式后，需要对字段进行去重操作
			py = unrepeatColumnName(py);
			
			createTableSB.append(", " + py + " varchar(1000)");//建表语句加字段
			
			insertSB.append( py +  ",");//insert语句加字段
		}
		insertSB.setLength(insertSB.length()-1);//  remove the comma at the end
		insertSB.append(") values(");
		for(int i = 0; i<rowlist.size() ; i++) {
			insertSB.append("?,");
		}
		insertSB.setLength(insertSB.length()-1);//  remove the comma at the end
		insertSB.append(")");
		PS = SQL_CONN.prepareStatement(insertSB.toString());
		
		createTableSB.append(")");
		
		System.out.println(createTableSB.toString());
		System.out.println(insertSB.toString());
		if(isCreateTable) {
			stat.executeUpdate(createTableSB.toString());//decide create table or not
		}
		
		stat.close();
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
	
	//java过滤utf8mb4表情符号
//	private static String filterUtf8Mb4(String origin) {
//		if(origin.trim().isEmpty()){
//			return origin;
//		}
//		String pattern="[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]";
//		String reStr="";
//		Pattern emoji=Pattern.compile(pattern);
//		Matcher  emojiMatcher=emoji.matcher(origin);
//		origin=emojiMatcher.replaceAll(reStr);
//		return origin;
//	}
	
	
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
	
	
//	
//	/**
//	 * 废弃的方法
//	 * @param cowSize
//	 * @throws Exception
//	 */
//	private static void createTable2(int cowSize) throws Exception {
//		
//		Connection conn = DriverManager.getConnection(URL, USERNAME, PASS);
//		Statement stat = conn.createStatement();
//		
//		StringBuilder sb = new StringBuilder();
//		sb.append("create table testExcel");
//		sb.append("(");
//		sb.append("id int primary key AUTO_INCREMENT");  // first title must be id, auto increment
//
//		for(int i = 0; i<cowSize ; i++) {
//			sb.append(", c" + i + " varchar(400)");
//		}
//		
//		sb.append(")");
//		
//		stat.executeUpdate(sb.toString());
//		stat.close();
//		conn.close();
//	}
	
}
