package com.yichimai.excel.dbfToCheck.hander;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 学业水平考试
 * 2021-12-27 把逻辑抽取出抽象类，后续的业务根据需要各自复写对应的抽象类方法
 * select ds_h,dsmc,xq_h,xqmc,kd_h,kdmc,kc_h,kmdm,kmmc,txsj,ksh,zwxh,xm,dtks from XX
 */
public class HandlerXysp extends Handler {

	public HandlerXysp() {
//		super();
		super(null, null, 4, 5, null, null, null, 6, 7, 8, 11, 12, 7, "学业水平考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() + item.getKmmc();
	}
	

}
