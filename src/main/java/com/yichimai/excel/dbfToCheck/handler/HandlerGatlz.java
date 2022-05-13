package com.yichimai.excel.dbfToCheck.handler;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 港澳台联合招生
 * 2022-05-13 把逻辑抽取出抽象类，后续的业务根据需要各自复写对应的抽象类方法
 * select 考点号,考点名称,科目号,科目名称,考场号,座位号,姓名 from lz order by 考点号,科目号,考场号,座位号
 */
public class HandlerGatlz extends Handler {

	public HandlerGatlz() {
		super();
//		super(null, null, 4, 5, null, null, null, 6, 7, 8, 11, 12, 7, "学业水平考试");
		setKdh(0).setKdmc(1).setKch(4).setKmdm(2).setKmmc(3).setZwh(5)
			.setXm(6).setSplitCodeInSchool(2).setExamName("港澳台联合招生考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() + "-" + item.getKmmc();
	}
	

}
