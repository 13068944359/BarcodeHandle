package com.yichimai.excel.dbfToCheck.hander;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linuxense.javadbf.DBFReader;
import com.yichimai.excel.dbfToExcel.DbfLineEntity;
import com.yichimai.excel.dbfToExcel.GenerateExcelUtil;

/**
 * 20211227 4.0版本
 * 抽取了学业水平考试的公共内容作为抽象类
 *
 */
public abstract class Handler {
	private static Logger LOGGER = LoggerFactory.getLogger(Handler.class);

	//各字段的下标
	private Integer dsh; //地市号
	private Integer xqh; //县区号
	private Integer kdh ;
	private Integer kdmc ;
	private Integer dzh ; //地址号
	private Integer dzmc ;//地址名称
	private Integer dy ; //单元号
	private Integer kch ;
	private Integer kmdm ;//科目代码
	private Integer kmmc ;
	private Integer zwh ;
	private Integer xm ;
	private Integer splitCodeInSchool;
	
	private String examName;
	GenerateExcelUtil excelUtil;
	List<String> logList;
	
	
	public Handler() {}
	
//	public Handler(Integer dsh, Integer xqh, Integer kdh, Integer kdmc, Integer dzh, Integer dzmc, Integer dy,
//			Integer kch, Integer kmdm, Integer kmmc, Integer zwh, Integer xm, Integer splitCodeInSchool, String examName) {
//		this.dsh = dsh;
//		this.xqh = xqh;
//		this.kdh = kdh;
//		this.kdmc = kdmc;
//		this.dzh = dzh;
//		this.dzmc = dzmc;
//		this.dy = dy;
//		this.kch = kch;
//		this.kmdm = kmdm;
//		this.kmmc = kmmc;
//		this.zwh = zwh;
//		this.xm = xm;
//		this.examName = examName;
//		this.splitCodeInSchool = splitCodeInSchool;
//	}
	
	
	public void handle(DBFReader reader,int pageSize,String targetFilePath,String encodeType, List<String> logList) throws Exception {
		this.logList = logList;
		excelUtil = new GenerateExcelUtil(encodeType,logList);
		excelUtil.initExcel(examName,pageSize,targetFilePath);
		handleSplitSchool(reader);
		excelUtil.flush();
	}
	
	List<String> targetStrList = null;
	private void handleSplitSchool(DBFReader reader) throws Exception {
		targetStrList = new LinkedList<>();
		
		String schoolCodeCache = null;
		Object[] rowValues;
		List<DbfLineEntity> schoolList = new LinkedList<DbfLineEntity>();
		
		boolean firstRecord = true;
		
		StringBuilder sb = new StringBuilder();
		while((rowValues = reader.nextRecord()) != null){
			if(firstRecord) {  //取第一行，验证文件编码
				excelUtil.generateEncodeTestLog(rowValues);//生成日志返回前台，方便检查编码格式
				firstRecord = false;
			}
			
			String currentSchoolCode = excelUtil.getValue(rowValues[this.kdh]);
			String kdmc = excelUtil.getValue(rowValues[this.kdmc]);
			String kch = excelUtil.getValue(rowValues[this.kch]);
			String kmdm = excelUtil.getValue(rowValues[this.kmdm]);
			String kmmc = this.kmmc==null? null: excelUtil.getValue(rowValues[this.kmmc]);
			String zwh = excelUtil.getValue(rowValues[this.zwh]);
			String xm = excelUtil.getValue(rowValues[this.xm]);
			String dy = this.dy==null? null: excelUtil.getValue(rowValues[this.dy]);
			String splitCodeInSchool = this.splitCodeInSchool==null? null: excelUtil.getValue(rowValues[this.splitCodeInSchool]);
			String dsh = this.dsh==null? null: excelUtil.getValue(rowValues[this.dsh]);
			String xqh = this.xqh==null? null: excelUtil.getValue(rowValues[this.xqh]);
			String dzh = this.dzh==null? null: excelUtil.getValue(rowValues[this.dzh]);
			String dzmc = this.dzmc==null? null: excelUtil.getValue(rowValues[this.dzmc]);
			if(schoolCodeCache == null) { schoolCodeCache = currentSchoolCode; }// first line
			
			if(!schoolCodeCache.equals(currentSchoolCode)) {
				handleBySchool(schoolList);
				excelUtil.writeSchoolInfoIntoExcel(targetStrList, schoolList.get(0).getKdh(), schoolList.get(0).getKdmc());
				schoolList = new LinkedList<DbfLineEntity>();//清空待处理缓存
				targetStrList = new LinkedList<>();//清空写入缓存
				schoolCodeCache = currentSchoolCode;
			}
			schoolList.add(new DbfLineEntity(dsh, xqh, currentSchoolCode, kdmc, dzh, dzmc, dy, kch, kmdm, kmmc, zwh, xm, splitCodeInSchool));
		}
		
		//最后的一个学校需要手动触发处理
		handleBySchool(schoolList);
		excelUtil.writeSchoolInfoIntoExcel(targetStrList, schoolList.get(0).getKdh(), schoolList.get(0).getKdmc());
	}
	
	private void handleBySchool(List<DbfLineEntity> schoolList) {
		Map<String, List<DbfLineEntity>> subjectMap = new HashMap<String, List<DbfLineEntity>>();
		
		//split by subject into map
		for(DbfLineEntity currentItem : schoolList) {
			String splitCodeInSchool = currentItem.getSplitCodeInSchool();
			
			if(subjectMap.containsKey(splitCodeInSchool)) {
				subjectMap.get(splitCodeInSchool).add(currentItem);
			}else {
				List<DbfLineEntity> subjectList = new LinkedList<DbfLineEntity>();
				subjectList.add(currentItem);
				subjectMap.put(splitCodeInSchool, subjectList);
			}
		}
		
		//handle data order by subject
		List<String> orderedSubjectCodeList = GenerateExcelUtil.getOrderedListByMapKeys(subjectMap);
		
		for(String currentSubjectCode : orderedSubjectCodeList) {
			List<DbfLineEntity> currentSubjectList = subjectMap.get(currentSubjectCode);
			List<String> classInfoFromSubject = getClassInfoFromSubjectOrDy(currentSubjectList);
			targetStrList.addAll(classInfoFromSubject);
			targetStrList.add("");//插入空行
		}
	}
	
	// 从已经划分好的科目集合里面，获取每个考场的首位名字信息
	private List<String> getClassInfoFromSubjectOrDy(List<DbfLineEntity> currentSubjectList) {
		List<String> targetClassInfoList = new LinkedList<>();
		
		Map<String, List<DbfLineEntity>> classMap = new HashMap<String, List<DbfLineEntity>>();
		//插入空行、科目行、标题行
		targetClassInfoList.add(generateSplitLine(currentSubjectList.get(0)));
		targetClassInfoList.add("考场-起始姓名-结束姓名-检查");
		
		//split by subject into map
		for(DbfLineEntity currentItem : currentSubjectList) {
			String kch = currentItem.getKch();
			
			if(classMap.containsKey(kch)) {
				classMap.get(kch).add(currentItem);
			}else {
				List<DbfLineEntity> classList = new LinkedList<DbfLineEntity>();
				classList.add(currentItem);
				classMap.put(kch, classList);
			}
		}

		//handle data order by ClassCode  先把班级之间先排序好，然后再获取班级内部
		List<String> orderedClassCodeList = GenerateExcelUtil.getOrderedListByMapKeys(classMap);
		for(String currentClassCode : orderedClassCodeList) {
			List<DbfLineEntity> currentClassList = classMap.get(currentClassCode);
			String currentClassLineStr = generateClassLine(currentClassList);
			targetClassInfoList.add(currentClassLineStr);
		}
		
		return targetClassInfoList;
	}
	
	//用于复写此方法
	abstract String generateSplitLine(DbfLineEntity item) ;
	
	
	//从班级集合中找到第一个人和最后一个人的姓名，拼装成所需的字符串
	public String generateClassLine(List<DbfLineEntity> classList) {
		StringBuilder sb = new StringBuilder();
		classList.sort((a,b) -> {  //按照座位号从小到大排序
			return Integer.parseInt(a.getZwh())< Integer.parseInt(b.getZwh())? -1 : 1; // order it from small to big
		});
		
		sb.append(classList.get(0).getKch());//考场号
		sb.append("-");
		sb.append(classList.get(0).getXm());//姓名
		sb.append("-");
		sb.append(classList.get(classList.size()-1).getXm());//姓名
		sb.append("- ");// add an extra space, make last cell have border
		return sb.toString();
	}


	public static void setLOGGER(Logger lOGGER) {
		LOGGER = lOGGER;
	}


	public Handler setDsh(Integer dsh) {
		this.dsh = dsh;
		return this;
	}


	public Handler setXqh(Integer xqh) {
		this.xqh = xqh;
		return this;
	}


	public Handler setKdh(Integer kdh) {
		this.kdh = kdh;
		return this;
	}


	public Handler setKdmc(Integer kdmc) {
		this.kdmc = kdmc;
		return this;
	}


	public Handler setDzh(Integer dzh) {
		this.dzh = dzh;
		return this;
	}


	public Handler setDzmc(Integer dzmc) {
		this.dzmc = dzmc;
		return this;
	}


	public Handler setDy(Integer dy) {
		this.dy = dy;
		return this;
	}


	public Handler setKch(Integer kch) {
		this.kch = kch;
		return this;
	}


	public Handler setKmdm(Integer kmdm) {
		this.kmdm = kmdm;
		return this;
	}


	public Handler setKmmc(Integer kmmc) {
		this.kmmc = kmmc;
		return this;
	}


	public Handler setZwh(Integer zwh) {
		this.zwh = zwh;
		return this;
	}

	public Handler setXm(Integer xm) {
		this.xm = xm;
		return this;
	}


	public Handler setSplitCodeInSchool(Integer splitCodeInSchool) {
		this.splitCodeInSchool = splitCodeInSchool;
		return this;
	}


	public Handler setExamName(String examName) {
		this.examName = examName;
		return this;
	}
	
	
	
}
