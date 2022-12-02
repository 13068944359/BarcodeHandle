package com.yichimai.excel.dbfToCheck.handler;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 术科考试（美术、书法、广编）
 * 2022-11-28 把逻辑抽取出抽象类，后续的业务根据需要各自复写对应的抽象类方法
 * SELECT ds_h,dsmc,xq_h,xqmc,kddm,kdmc,kc_h,kmdm,kmmc,txsj,ksh,zwxh,xm,txms FROM sfbp ORDER BY ds_h,xq_h,kddm,kmdm,kc_h,zwxh
 * 
 */
public class HandlerSk extends Handler {

	public HandlerSk() {
		super();
//		super(null, null, 4, 5, null, null, null, 6, 7, 8, 11, 12, 7, "学业水平考试");
		setKdh(4).setKdmc(5).setKch(6).setKmdm(7).setKmmc(8).setZwh(11)
			.setXm(12).setSplitCodeInSchool(7).setExamName("高考术科考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() + item.getKmmc();
	}
	

}
