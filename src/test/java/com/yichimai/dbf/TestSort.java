package com.yichimai.dbf;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TestSort {

	public static void main(String[] args) {
		List<String> subjectList = new LinkedList<>();
		subjectList.add("03");
		subjectList.add("07");
		subjectList.add("04");
		subjectList.add("02");
		System.out.println(subjectList);
		subjectList.sort((a,b) -> {
			return Integer.parseInt(a)< Integer.parseInt(b)? 1 : -1;
		});
		
		System.out.println(subjectList);
		

		String colTitles = "申报考试地点_地市,现居住市县,详细地址,考生号,姓名,联系电话,考试类型,考生类别,学历类别,毕业院校";

		String[] colTitlesArray = colTitles.split(",");
		
		System.out.println(colTitlesArray[0]);
		
		String a = String.valueOf(null);
		System.out.println(a);
	}
}
