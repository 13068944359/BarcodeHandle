package com.yichimai.excel.dbfToCheck.handler;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 中职专业技能
 * 2021-12-27
 * select xh,ds_h,dsmc,xq_h,xqmc,kddm,kdmc,kmdm,kmmc,dtkys,tmsj,ksh,xm,kcdm,zw from A order by xh
 */
public class HandleZz extends Handler {

	public HandleZz() {
//		super(1, 3, 5, 6, null, null, null, 14, 7, 8, 13, 12, 7, "中等职业技术教育专业技能考试");
		super();
		setDsh(1).setXqh(3).setKdh(5).setKdmc(6).setKch(13).setKmdm(7).setKmmc(8).
			setZwh(14).setXm(12).setSplitCodeInSchool(7).setExamName("中等职业技术教育专业技能考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "科目-" + item.getKmdm() + item.getKmmc();
	}
}
