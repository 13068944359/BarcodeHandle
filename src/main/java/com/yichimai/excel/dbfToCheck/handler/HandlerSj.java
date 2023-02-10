package com.yichimai.excel.dbfToCheck.handler;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 三+证书考试
 * select kddm,kdmc,kc_h,kmdm,kmmc,zwxh,xm from sj order by kddm,kmdm,kc_h,zwxh
 */
public class HandlerSj extends Handler {

	public HandlerSj() {
		super();
//		super(null, null, 4, 5, null, null, null, 6, 7, 8, 11, 12, 7, "学业水平考试");
		setKdh(0).setKdmc(1).setKch(2).setKmdm(3).setKmmc(4).setZwh(5)
			.setXm(6).setSplitCodeInSchool(3).setExamName("三+证书考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() + item.getKmmc();
	}
	

}
