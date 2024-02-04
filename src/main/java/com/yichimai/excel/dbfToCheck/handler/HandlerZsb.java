package com.yichimai.excel.dbfToCheck.handler;

import java.util.List;

import com.yichimai.excel.dbfToCheck.DbfLineEntity;

/**
 * 本考试需要特殊处理：
 * 因为考试时间需要手动转换成支持排序的内容
 * 
 * 
 * 专升本
 * 2022-04-21 把逻辑抽取出抽象类，后续的业务根据需要各自复写对应的抽象类方法
 * select kd_h,kdmc,考试时间,kc_h,km_h,科目名称,zw_h,姓名 from A order by kd_h,考试时间,kc_h,km_h,zw_h
 * 
 * ***注意：单元号不是纯数字，需要替换
 * UPDATE jdb SET 考试时间='1' WHERE 考试时间='2023-03-25 08:00,2023-03-25 10:00       '
UPDATE jdb SET 考试时间='2' WHERE 考试时间='2023-03-25 10:30,2023-03-25 12:30'
UPDATE jdb SET 考试时间='3' WHERE 考试时间='2023-03-25 15:00,2023-03-25 17:00     '
UPDATE jdb SET 考试时间='4' WHERE 考试时间='2023-03-26 09:00,2023-03-26 11:30         '
UPDATE jdb SET 考试时间='5' WHERE 考试时间='2023-03-26 09:00,2023-03-26 12:30    '
 */
public class HandlerZsb extends Handler {

	public HandlerZsb() {
		super();
//		super(null, null, 4, 5, null, null, null, 6, 7, 8, 11, 12, 7, "学业水平考试");
		setKdh(0).setKdmc(1).setDy(2).setKch(3).setKmdm(4).setKmmc(5).setZwh(6)
			.setXm(7).setSplitCodeInSchool(2).setExamName("专升本考试");
	}
	
	String generateSplitLine(DbfLineEntity item) {
		String dyStr = null;
		switch (Integer.valueOf(item.getDy())) {
		case 1:
			dyStr = "25日 08:00~10:00";
			break;
		case 2:
			dyStr = "25日 10:30~12:30";
			break;
		case 3:
			dyStr = "25日 15:00~17:00";
			break;
		case 4:
			dyStr = "26日 09:00~11:30";
			break;
		case 5:
			dyStr = "26日 09:00~12:30";
			break;

		default:
			System.out.println("异常数据");
			break;
		}
		
		return "单元-" + dyStr ;
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
