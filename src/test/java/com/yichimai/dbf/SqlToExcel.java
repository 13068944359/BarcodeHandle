package com.yichimai.dbf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import com.yichimai.dbutil.DBUtil;

public class SqlToExcel {

	
	
	public static void main(String[] args) throws Exception {
		DBUtil.init("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/dbf?characterEncoding=utf8&useSSL=true",
				"root", "root", 3000, "select '1' from abc");

		String oldSql = "select * from zsb20220425" ;
		
		File fout = new File("D:/tmp/out.txt");
		FileOutputStream fos = new FileOutputStream(fout);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
//		List<Map<String, Object>> records = DBUtil.queryMapList(oldSql, null);
		List<Object[]> records = DBUtil.queryArrayList(oldSql, null);
		System.out.println(records.size());
		for(Object[] r : records) {
			write(bw, r);
			bw.newLine();
		}
		
		bw.flush();
		bw.close();
	}
	
	private static void write(BufferedWriter bw , Object[] r) throws Exception {
		
		String target  = "";
		
		for(Object o : r) {
			target += (o==null?" ":o.toString()) + "\t";
		}
		
		bw.write(target);
	}
	
	
	
}
