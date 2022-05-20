package com.yichimai.excel.dbfToCheck.handler;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 普通高考
 * 2022-05-20 
 * select kddm,kdmc,kmdm,kmmc,kc_h,zwxh,xm from pgksbp order by kddm,kmdm,kc_h,zwxh
 */
public class HandlerGk extends Handler {

	public HandlerGk() {
		super();
//		super(null, null, 4, 5, null, null, null, 6, 7, 8, 11, 12, 7, "学业水平考试");
		setKdh(0).setKdmc(1).setKch(4).setKmdm(2).setKmmc(3).setZwh(5)
			.setXm(6).setSplitCodeInSchool(2).setExamName("普通高考");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() + "-" + item.getKmmc();
	}
	

}
