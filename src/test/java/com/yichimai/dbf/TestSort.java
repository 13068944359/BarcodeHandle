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
	}
}
