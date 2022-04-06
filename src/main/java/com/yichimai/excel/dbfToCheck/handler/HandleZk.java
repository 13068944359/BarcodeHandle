package com.yichimai.excel.dbfToCheck.handler;

import java.util.List;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 自考条形码
 * 
 * 特殊的地方在于，考点内按照单元进行划分，且由于考场内有科目混在一起的情况
 * 所以考场内数据还要根据科目进行排序
 * 
 select 考点代码,考点名称,节点单元,round(考场代码,0),round(科目代码,0),round(座位号,0),姓名 from 
 ( select 考点代码,考点名称,节点单元,val(考场代码) 考场代码, val(科目代码) 科目代码,val(座位号) 座位号,姓名 from zk ) t 
 ORDER BY 考点代码,节点单元,考场代码,科目代码,座位号
 */
public class HandleZk extends Handler {

	public HandleZk() {
//		super(null, null, 0, 1, null, null, 2, 3, 4, null, 5, 6, 2, "自学考试");
		super();
		setKdh(0).setKdmc(1).setDy(2).setKch(3).setKmdm(4)
			.setZwh(5).setXm(6).setSplitCodeInSchool(2).setExamName("自学考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		return "单元-" + item.getDy() ;
	}

	
	/**
	 * 这里用了方法重载，后续注意排查此处可能会出BUG
	 */
	//从班级集合中找到第一个人和最后一个人的姓名，拼装成所需的字符串
	@Override
	public String generateClassLine(List<DbfLineEntity> classList) {
		StringBuilder sb = new StringBuilder();
		classList.sort((a,b) -> {  //按照座位号从小到大排序
			if(Integer.parseInt(a.getKmdm())< Integer.parseInt(b.getKmdm())) {   //首先根据科目排序，再根据座位号
				return -1;
			}else if(Integer.parseInt(a.getKmdm())> Integer.parseInt(b.getKmdm())) {
				return 1;
			}else {
				return Integer.parseInt(a.getZwh())< Integer.parseInt(b.getZwh())? -1 : 1;// order it from small to big
			}
		});
		
		sb.append(classList.get(0).getKch());//考场号
		sb.append("-");
		sb.append(classList.get(0).getXm());//姓名
		sb.append("-");
		sb.append(classList.get(classList.size()-1).getXm());//姓名
		sb.append("- ");// add an extra space, make last cell have border
		return sb.toString();
	}
	
}
