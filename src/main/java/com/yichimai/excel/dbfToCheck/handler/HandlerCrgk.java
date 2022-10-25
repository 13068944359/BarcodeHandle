package com.yichimai.excel.dbfToCheck.handler;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 成人高考
 * 2022-10-25 把逻辑抽取出抽象类，后续的业务根据需要各自复写对应的抽象类方法
 * SELECT kcdm,kcmc,ssh,kmdm,kmmc,zwh,xm FROM ck20221024 ORDER BY kcdm,kmdm,ssh,zwh
 * 
 * 
 */
public class HandlerCrgk extends Handler {

	public HandlerCrgk() {
		super();
//		super(null, null, 4, 5, null, null, null, 6, 7, 8, 11, 12, 7, "学业水平考试");
		setKdh(0).setKdmc(1).setKch(2).setKmdm(3).setKmmc(4).setZwh(5)
			.setXm(6).setSplitCodeInSchool(3).setExamName("成人高考");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() + item.getKmmc();
	}
	

}
