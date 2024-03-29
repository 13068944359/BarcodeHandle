package com.yichimai.excel.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.yichimai.excel.controller.DbfToDatabaseController;

public class DbfToDatabaseUtilTest {

	private static Logger LOGGER = LoggerFactory.getLogger(DbfToDatabaseController.class);
	
	public static void handle(String databaseType,String mysqlUrl,String mysqlUser,String mysqlPass,String newTableName,
			Boolean isCreateTable,MultipartFile dbfFile,String charset) throws Exception {

		loadDBDriver(databaseType);
		DBFReader reader = new DBFReader(dbfFile.getInputStream(),Charset.forName(charset));// specify encoding method
		
		int fieldsCount = reader.getFieldCount();//get title msg
		//取出字段信息
		List<String> titleList = new ArrayList<String>();
		for( int i=0; i<fieldsCount; i++){
			DBFField field = reader.getField(i);
			titleList.add(field.getName());
		}
//		System.out.println(titleList);//print title
		LOGGER.info("dbfTitle= " + titleList);
		
		if(isCreateTable) {
			createTable(mysqlUrl, mysqlUser, mysqlPass, titleList, newTableName);
		}
		
		insertData(reader, mysqlUrl, mysqlUser, mysqlPass, titleList, newTableName);
	}
	
	// handle multi file
	public static void handle(String databaseType,String mysqlUrl,String mysqlUser,String mysqlPass,String newTableName,
			Boolean isCreateTable,MultipartFile[] dbfFile,String charset) throws Exception {
		
		loadDBDriver(databaseType);
		DBFReader reader = new DBFReader(dbfFile[0].getInputStream(),Charset.forName(charset));// specify encoding method
		
		int fieldsCount = reader.getFieldCount();//get title msg
		//取出字段信息
		List<String> titleList = new ArrayList<String>();
		for( int i=0; i<fieldsCount; i++){
			DBFField field = reader.getField(i);
			titleList.add(field.getName());
		}
//		System.out.println(titleList);//print title
		LOGGER.info("dbfTitle= " + titleList);
		
		if(isCreateTable) {
			createTable(mysqlUrl, mysqlUser, mysqlPass, titleList, newTableName);
		}
		reader.close();
		
		for(MultipartFile f : dbfFile) {
			reader = new DBFReader(f.getInputStream(),Charset.forName(charset));// specify encoding method
			insertData(reader, mysqlUrl, mysqlUser, mysqlPass, titleList, newTableName);
			reader.close();
		}
	}
	
	
	private static void loadDBDriver(String databaseType) throws Exception {
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
	}

	
	private static void insertData(DBFReader reader,String mysqlUrl,String mysqlUser,String mysqlPass,
			List<String> titleList,String tableName) throws Exception {
		Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
		conn.setAutoCommit(false);//must not be auto commit
		
		/**
		 * create the insert sql statement
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("insert into " + tableName );
		sb.append("(");
		for(String title:titleList) {
			sb.append(title +  ",");
		}
		sb.setLength(sb.length()-1);//  remove the comma at the end
		sb.append(")");
		sb.append("values(");
		for(String title:titleList) {
			sb.append("?,");
		}
		sb.setLength(sb.length()-1);//  remove the comma at the end
		sb.append(")");
		
		PreparedStatement ps = conn.prepareStatement(sb.toString());
		
		
		int lineCount = 0;
		long countAll = 0;
		Object[] rowValues;
		while((rowValues = reader.nextRecord()) != null){
			for(int index=0; index<titleList.size(); index++	) {
				String val = rowValues[index]==null?"":rowValues[index].toString().trim();
				ps.setString(index+1 , val);
			}
			ps.addBatch(); // one record in batch
			lineCount ++; countAll ++;
			
			if(lineCount == 5000) {
				AsyncInsertData(ps);
//				ps.executeBatch();// insert data in batch
				ps = conn.prepareStatement(sb.toString());//上一个对象还在异步线程中阻塞，这里直接新建一个ps来执行下一次insert
				lineCount = 0;// reset count
				System.out.println("already handle data count= " + countAll);
			}
		}
		
		if(lineCount != 0) {
			ps.executeBatch();// insert data in batch
		}
		conn.commit();
		ps.close();
		conn.close();
	}
	
	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
	private static void AsyncInsertData(PreparedStatement ps) {
		PreparedStatement currentPs = ps;
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				try {
					currentPs.executeBatch();// insert data in batch
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}
	
	
	private static void createTable(String mysqlUrl,String mysqlUser,String mysqlPass,
			List<String> titleList,String tableName) throws Exception {
		Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
		Statement stat = conn.createStatement();
		
		StringBuilder sb = new StringBuilder();
		sb.append("create table " + tableName);
		sb.append("(");
//		sb.append("id int primary key AUTO_INCREMENT");  // first title must be id, auto increment

		LOGGER.info("create table " + tableName);
		for(String title : titleList ) {
			sb.append(title + " varchar(50)" + ",");
		}
		sb.setLength(sb.length()-1);//去除逗号
		sb.append(")");
		
		
		stat.executeUpdate(sb.toString());
		stat.close();
		conn.close();
	}
	
	

	
	
	/**
	 * 检查数据库字段类型和长度信息
	 * 打印
	 * 字段名	字段类型	长度
	 */
	/*public static void check() {
		InputStream fis = null;
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("D://check20200929.xiaojz"),false));
			
			//读取文件的输入流ba
			fis = new FileInputStream("D:\\tmp\\202106高考成绩查询测试\\ksy_ytl_zf\\test_1_1.dbf");
			DBFReader reader = new DBFReader(fis);
			
			int fieldsCount = reader.getFieldCount();
			System.out.println("字段数:"+fieldsCount);
			//取出字段信息
			for( int i=0; i<fieldsCount; i++){
				DBFField field = reader.getField(i);
				
				String fieldType = null;
				if(field.getType().toString().equals("CHARACTER")) {
					fieldType = "字符型";
				}else if(field.getType().toString().equals("NUMERIC")){
					fieldType = "数值型";
				}
				
				System.out.println(field.getName() + "\t" + fieldType + "\t" + field.getLength());
			}
			
		}
		catch(Exception e)	{
			e.printStackTrace();
		}finally{
			try {
				fis.close();
			}catch(Exception e){}
		}
		
		
	}*/
	
//	private static String getValue(Object o) throws Exception {
//		return o==null?"":new String(o.toString ().trim().getBytes ("ISO-8859-1"),"GBK");
//	}
	
	
}
