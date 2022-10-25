package com.yichimai.excel.dbfToCheck.handler;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 中职专业技能
 * 2021-12-27
 * select kddm,kdmc,kmdm,kmmc,kcdm,zw,xm from A order by kddm,kmdm,kcdm,zw
 */
public class HandleZz extends Handler {

	public HandleZz() {
//		super(1, 3, 5, 6, null, null, null, 14, 7, 8, 13, 12, 7, "中等职业技术教育专业技能考试");
		super();
		setKdh(0).setKdmc(1).setKch(4).setKmdm(2).setKmmc(3).
			setZwh(5).setXm(6).setSplitCodeInSchool(2).setExamName("中等职业技术教育专业技能考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() + item.getKmmc();
	}
}
