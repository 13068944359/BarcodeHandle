package com.yichimai.excel.dbfToExcel;

public class DbfLineEntity {
	private String dsh; //地市号
	private String xqh; //县区号
	private String kdh ;
	private String kdmc ;
	private String dzh ; //地址号
	private String dzmc ;//地址名称
	private String dy ; //单元号
	private String kch ;
	private String kmdm ;//科目代码
	private String kmmc ;
	private String zwh ;
	private String xm ;
	private String splitCodeInSchool;//增加额外一个字段用来存储是按照科目分组还是单元分组，在初始化时指定这个字段的下标
	
	public DbfLineEntity(String dsh, String xqh, String kdh, String kdmc, String dzh, String dzmc, String dy, String kch,
			String kmdm, String kmmc, String zwh, String xm, String splitCodeInSchool) {
		super();
		this.dsh = dsh;
		this.xqh = xqh;
		this.kdh = kdh;
		this.kdmc = kdmc;
		this.dzh = dzh;
		this.dzmc = dzmc;
		this.dy = dy;
		this.kch = kch;
		this.kmdm = kmdm;
		this.kmmc = kmmc;
		this.zwh = zwh;
		this.xm = xm;
		this.splitCodeInSchool = splitCodeInSchool;
	}

	public String getSplitCodeInSchool() {
		return splitCodeInSchool;
	}
	
	public String getDsh() {
		return dsh;
	}

	public String getXqh() {
		return xqh;
	}

	public String getKdh() {
		return kdh;
	}

	public String getKdmc() {
		return kdmc;
	}

	public String getDzh() {
		return dzh;
	}

	public String getDzmc() {
		return dzmc;
	}

	public String getDy() {
		return dy;
	}

	public String getKch() {
		return kch;
	}

	public String getKmdm() {
		return kmdm;
	}

	public String getKmmc() {
		return kmmc;
	}

	public String getZwh() {
		return zwh;
	}

	public String getXm() {
		return xm;
	}

	@Override
	public String toString() {
		return "DbfLine [dsh=" + dsh + ", xqh=" + xqh + ", kdh=" + kdh + ", kdmc=" + kdmc + ", dzh=" + dzh + ", dzmc="
				+ dzmc + ", dy=" + dy + ", kch=" + kch + ", kmdm=" + kmdm + ", kmmc=" + kmmc + ", zwh=" + zwh + ", xm="
				+ xm + "]";
	}
	
}
