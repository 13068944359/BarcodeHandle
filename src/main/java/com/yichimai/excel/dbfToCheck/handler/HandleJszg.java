package com.yichimai.excel.dbfToCheck.handler;

import java.util.List;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 教师资格
 * 2022-02-25
 * select sfdm,kqdm,kddm,kdmc,kmdm,kmmc,kcdm,kczwh,zkzh,ksxm from jszg order by sfdm,kqdm,kddm,kmdm,kcdm,kczwh
 * 
 */
public class HandleJszg  extends Handler {

	public HandleJszg() {
		super();
		setKdh(2).setKdmc(3).setKch(6).setKmdm(4).setKmmc(5).setZwh(7)
			.setXm(9).setSplitCodeInSchool(4).setExamName("教师资格考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() ;
	}
	
	
	/**
	 * 教师资格证需要特殊处理考场号，考场号过长，需要截取
	 */
	@Override
	public String generateClassLine(List<DbfLineEntity> classList) {
		StringBuilder sb = new StringBuilder();
		classList.sort((a,b) -> {  //按照座位号从小到大排序
			return Integer.parseInt(a.getZwh())< Integer.parseInt(b.getZwh())? -1 : 1; // order it from small to big
		});
		
		sb.append(classList.get(0).getKch().substring(6,9));//考场号
		sb.append("-");
		sb.append(classList.get(0).getXm());//姓名
		sb.append("-");
		sb.append(classList.get(classList.size()-1).getXm());//姓名
		sb.append("- ");// add an extra space, make last cell have border
		return sb.toString();
	}
	
}
