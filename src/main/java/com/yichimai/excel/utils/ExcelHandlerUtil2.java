package com.yichimai.excel.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.yichimai.excel.controller.DbfToDatabaseController;

/**
 * 2.0 升级版
 * 2021-10-19
 * 为了精简纸张，优化展示内容
 * 对版面内容进行大幅度升级
 * 1.省份、考区、考点等信息，只在该页面的头部展示一次，无需在每一行重复打印
 * 2.其他信息（科目、考场、首尾考生姓名等信息），可以在一页内容中放置多列
 * 
 * 目前已经涵盖的考试：
 * 教师资格证考试
 * 学业水平考试（院校的index不同）
 * 研究生考试：虽然多了“单元划分”，但是仅凭科目即可保证唯一，因此同样使用科目进行划分（2021-12-09）
 * 
 * 2021-12-14 增加了多个字段划分学校的逻辑（多个字段用逗号分割，会被合并在一起打印在页头）
 * 决定去掉“横向”的打印方式，行高统一为12.5
 *
 */
public class ExcelHandlerUtil2 {
	private static Logger LOGGER = LoggerFactory.getLogger(ExcelHandlerUtil2.class);
	private static float HEIGHT_PT =  (float)14.25;//每一行的高度
	private static int NEW_SHEET_LINE_INDEX = 0;
	
	public static void paging(String pageTitle,String examType,int sheetIndex,String spilitSchoolCellIndex,boolean landscape,
			int pageSize,MultipartFile uploadFile,String targetFilePath) throws Exception {
		
	    String fileName = uploadFile.getOriginalFilename();// 获取文件名
	    String suffixName = fileName.substring(fileName.lastIndexOf("."));// 获取后缀
		
	    Workbook wb = null;
	    if(".xls".equals(suffixName)){
	    	wb = new HSSFWorkbook(uploadFile.getInputStream());
        }else if(".xlsx".equals(suffixName)){
        	SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(uploadFile.getInputStream()));
            wb = workbook.getXSSFWorkbook();
        }else{
            throw new Exception("file type error");
        }
	    
	    NEW_SHEET_LINE_INDEX = 0;//初始化必要变量
//	    SXSSFWorkbook  // 用于处理大数据量以及超大数据量的导出
		SXSSFWorkbook newWorkbook=new SXSSFWorkbook();//创建Excel文件薄
        SXSSFSheet newSheet=newWorkbook.createSheet();//创建工作表sheeet
        
        CellStyle newCellStyle = createCommonStyle(newWorkbook);//create common cell style
        
        PrintSetup ps = newSheet.getPrintSetup();
        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
        ps.setLandscape(landscape); // 打印方向，true：横向，false：纵向
//        newSheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));//设置页眉为表格头 //2.0版本表格头要求显示对应的学校代码和名称，因此需要手动实现
		
        if(pageTitle != null) {
        	Header header = newSheet.getHeader();
        	header.setCenter(pageTitle);
        }
        
        Footer newFooter = newSheet.getFooter();
        newFooter.setCenter(HSSFFooter.page()+" / "+HSSFFooter.numPages());
        newFooter.setLeft("禁止先勾后检查，禁止连续勾几行");
        newFooter.setRight("技术支持：xiaojz");
        
        handle_split_by_school(examType, wb, sheetIndex, spilitSchoolCellIndex, pageSize, newWorkbook, newSheet, newCellStyle);
        
        FileOutputStream fileOut = new FileOutputStream(targetFilePath);
        newWorkbook.write(fileOut);
        newWorkbook.close();
        fileOut.close();
	}
	
	private static void handle_split_by_school(String examType,Workbook wb,int sheetIndex,String spilitSchoolCellIndex,int pageSize,
			SXSSFWorkbook newWorkbook,SXSSFSheet newSheet,CellStyle newCellStyle) throws Exception {
		int newSheetLineIndex = 0;
    	Sheet sheet = wb.getSheetAt(sheetIndex);//获取sheet对象 
//    	String currentSchoolCode = sheet.getRow(1).getCell(schoolCodeCellIndex).getStringCellValue();//考点代码（用来判断是否变化）
    	String splitCodeCache = getSplitCode(sheet.getRow(1), spilitSchoolCellIndex);//优化逻辑，不局限于“学校代码”
    	
    	List<Row> schoolListCache = new LinkedList<Row>();// 缓存一个学校的数据
    	 // 返回该页的总行数
    	int lastRowNum=sheet.getLastRowNum();
    	for (int lineIndex = 1; lineIndex <= lastRowNum; lineIndex++) {  //ineIndex <= lastRowNum 是因为下标从1开始而不是0，不补1，会缺少最后一行
    		Row currentRow=sheet.getRow(lineIndex);
        	
//        	String schoolCode = currentRow.getCell(schoolCodeCellIndex).getStringCellValue();
    		String currentSplitCode = getSplitCode(currentRow, spilitSchoolCellIndex);//优化逻辑，不局限于“学校代码”
        	/**
        	 * 循环读取，把一个考点的记录读取到一个list中
        	 * 读取到下一个考点，则说明已经读取完毕
        	 * 再处理下一个考点之前，先对已有记录的list进行处理，并清空
        	 */
        	if(currentSplitCode.equals(splitCodeCache)) {
        		schoolListCache.add(currentRow);// add item to listCache
        	}else {
        		handle_school_data(examType, sheetIndex, spilitSchoolCellIndex, schoolListCache, pageSize, newSheet,newCellStyle);
        		
        		splitCodeCache = currentSplitCode;
        		schoolListCache.clear();// 清空缓存
        		schoolListCache.add(currentRow);// add item to listCache
        	}
        }
    	handle_school_data(examType, sheetIndex, spilitSchoolCellIndex, schoolListCache, pageSize, newSheet,newCellStyle);
	}
	
	
	private static void handle_school_data(String examType,int sheetIndex,String spilitSchoolCellIndex,
			 List<Row> schoolListCache,int pageSize,
			SXSSFSheet newSheet,CellStyle newCellStyle) throws Exception {
		
		switch (examType) {
		case "jszg":
			handle_school_jszgz(  sheetIndex, spilitSchoolCellIndex, schoolListCache, pageSize, newSheet,newCellStyle);
			break;
			
		case "sk":
			handle_school_sk(  sheetIndex, spilitSchoolCellIndex, schoolListCache, pageSize, newSheet,newCellStyle);
			break;

		case "sy":
			handle_school_sy(  sheetIndex, spilitSchoolCellIndex, schoolListCache, pageSize, newSheet,newCellStyle);
			break;
		default:
            throw new Exception("examType error");
		}
	}
	
	private static void handle_school_sk(int sheetIndex,String spilitSchoolCellIndex, List<Row> schoolListCache,int pageSize, SXSSFSheet newSheet,CellStyle newCellStyle) throws Exception {
		int columnInColumnSize = 5; //在新版式里面的一个大列里面有多少小列
		int columnSize = 3; //在新版式里面有 多少个大列
		int subjectCodeIndex = 6;
		int subjectNameIndex = 7;
		boolean showSubjectName = true; //是否打印科目名称
		int classIndex = 8;
		int firstNameIndex = 9;
		int lastNameIndex = 10;
		
		handle_school_common(sheetIndex, spilitSchoolCellIndex, schoolListCache, pageSize, newSheet, newCellStyle, columnInColumnSize, columnSize, subjectCodeIndex, subjectNameIndex, showSubjectName, classIndex, firstNameIndex, lastNameIndex, "科目");
	}
	
	//硕士研究生考试
	private static void handle_school_sy(int sheetIndex,String spilitSchoolCellIndex, List<Row> schoolListCache,int pageSize, SXSSFSheet newSheet,CellStyle newCellStyle) throws Exception {
			int columnInColumnSize = 5; //在新版式里面的一个大列里面有多少小列
			int columnSize = 3; //在新版式里面有 多少个大列
			int subjectCodeIndex = 4;
			int subjectNameIndex = 5;
			boolean showSubjectName = false; //是否打印科目名称
			int classIndex = 5;
			int firstNameIndex = 7;
			int lastNameIndex = 8;
			
			handle_school_common(sheetIndex, spilitSchoolCellIndex, schoolListCache, pageSize, newSheet, newCellStyle, columnInColumnSize, columnSize, subjectCodeIndex, subjectNameIndex, showSubjectName, classIndex, firstNameIndex, lastNameIndex, "单元");
		}
	
	
	private static void handle_school_jszgz(int sheetIndex,String spilitSchoolCellIndex, List<Row> schoolListCache,int pageSize, SXSSFSheet newSheet,CellStyle newCellStyle) throws Exception {
		int columnInColumnSize = 5; //在新版式里面的一个大列里面有多少小列
		int columnSize = 3; //在新版式里面有 多少个大列
		int subjectCodeIndex = 5;
		int subjectNameIndex = 6;
		boolean showSubjectName = false; //是否打印科目名称
		int classIndex = 7;
		int firstNameIndex = 8;
		int lastNameIndex = 9;
		
		handle_school_common(sheetIndex, spilitSchoolCellIndex, schoolListCache, pageSize, newSheet, newCellStyle, columnInColumnSize, columnSize, subjectCodeIndex, subjectNameIndex, showSubjectName, classIndex, firstNameIndex, lastNameIndex, "科目");
	}
	
	
	/**
	 * 抽取公共办法
	 * @param columnInColumnSize  在新版式里面的一个大列里面有多少小列
	 * @param columnSize 在新版式里面有 多少个大列
	 * 2021-12-10 有些不一定使用科目，所以使用subjectName自定义
	 */
	private static void handle_school_common(int sheetIndex,String spilitSchoolCellIndex,
			 List<Row> schoolListCache,int pageSize,
			SXSSFSheet newSheet,CellStyle newCellStyle,
			int columnInColumnSize,int columnSize,
			int subjectCodeIndex, int subjectNameIndex,boolean showSubjectName,
			int classIndex,int firstNameIndex,int lastNameIndex,String subjectName) throws Exception {
		//设置好列宽
		newSheet.setDefaultColumnWidth(7*256);//设置7个字符的宽度（7个数字）
		newSheet.setColumnWidth(columnInColumnSize*0 + 0, 5*256);//第一列
		newSheet.setColumnWidth(columnInColumnSize*1 + 0, 5*256);
		newSheet.setColumnWidth(columnInColumnSize*2 + 0, 5*256);

		newSheet.setColumnWidth(columnInColumnSize*0 + 3, 5*256);//“检查”列
		newSheet.setColumnWidth(columnInColumnSize*1 + 3, 5*256);
		newSheet.setColumnWidth(columnInColumnSize*2 + 3, 5*256);

		newSheet.setColumnWidth(columnInColumnSize*0 + 4, 2*256);//空白列
		newSheet.setColumnWidth(columnInColumnSize*1 + 4, 2*256);
		
		/**
		 * 数据的预处理，先把数据加工好，再去填充新sheet
		 * 4种类型：科目、标题头、姓名、空行
		 */
		List<String> targetStrList = new LinkedList<>();
		String subjectCodeCache = null;//教师资格证科目代码的index是5，标志位判断科目是否发生变化
		StringBuilder sb = new StringBuilder();
		for(Row rowOfSchool : schoolListCache) {
			String currentSubjectCode = rowOfSchool.getCell(subjectCodeIndex).getStringCellValue();
			String currentSubjectName = showSubjectName? "-" + rowOfSchool.getCell(subjectNameIndex).getStringCellValue():"";
			if(!currentSubjectCode.equals(subjectCodeCache)) {
				subjectCodeCache = currentSubjectCode;
				//插入空行、科目行、标题行
				targetStrList.add("");
				targetStrList.add(subjectName + "-" + currentSubjectCode + currentSubjectName);
				targetStrList.add("考场-起始姓名-结束姓名-检查");
			}
			sb.setLength(0);// clear
			sb.append(rowOfSchool.getCell(classIndex).getStringCellValue().toString());//考场号
			sb.append("-");
			sb.append(rowOfSchool.getCell(firstNameIndex).getStringCellValue().toString());//姓名
			sb.append("-");
			sb.append(rowOfSchool.getCell(lastNameIndex).getStringCellValue().toString());//姓名
			sb.append("- ");// add an extra space, make last cell have border
			targetStrList.add(sb.toString());
		}
		targetStrList.remove(0);//去掉第一个空行
		
		List<SXSSFRow> pageList = new LinkedList<>();
		int rowIndexOfPageList = 1; //记录已经写到第几行 0开始（由于第一行默认是院校代码，所以从1开始）
		int columnIndexOfPageList = 0;//记录已经写到第几列 0开始
		boolean evenPage = false;//判断是否偶数页码？不是就需要补充一页空白

		createNewPage(pageSize, spilitSchoolCellIndex, newSheet, pageList, schoolListCache, columnInColumnSize,evenPage);
		for(String targetStr : targetStrList) {
			if(targetStr.length() == 0) {
				//空白行
			}else {
				String[] split = targetStr.split("-");
				for(int index = 0;index < split.length; index++) {
					pageList.get(rowIndexOfPageList).getCell(columnIndexOfPageList*columnInColumnSize + index).setCellValue(split[index]);
					pageList.get(rowIndexOfPageList).getCell(columnIndexOfPageList*columnInColumnSize + index).setCellStyle(newCellStyle);
				}
			}
			
			rowIndexOfPageList ++;
			if(rowIndexOfPageList == pageSize) {
				rowIndexOfPageList = 1;//重新开始的1行
				columnIndexOfPageList++;
				if(columnIndexOfPageList == columnSize) {
					columnIndexOfPageList = 0;//重新开始的1页，从0列开始
					//创建新开始的1页
					evenPage = !evenPage;//标记是否偶数页
					createNewPage(pageSize, spilitSchoolCellIndex, newSheet, pageList, schoolListCache, columnInColumnSize,evenPage);
				}
			}
		}
		if(!evenPage) {//不是偶数页
			evenPage = !evenPage;//标记是否偶数页
			createNewPage(pageSize, spilitSchoolCellIndex, newSheet, pageList, schoolListCache, columnInColumnSize,evenPage);
		}
	}
	
	private static String getSplitCode(Row r,String spilitSchoolCellIndex) {
		String[] split = spilitSchoolCellIndex.split(",");//用英文逗号作分隔符
		String result = "";
		for(String s : split) {
			result += r.getCell(Integer.valueOf(s)).getStringCellValue() + "-";
		}
		return result.substring(0, result.length()-1);
	}
	
	private static void createNewPage(int pageSize,String spilitSchoolCellIndex,SXSSFSheet newSheet,
			List<SXSSFRow> pageList, List<Row> schoolListCache, int columnSize, boolean evenPage) {
		pageList.clear();
		for(int count = 0;count < pageSize; count++) {  //先创建一整页空行
			SXSSFRow newSheetRow = newSheet.createRow(NEW_SHEET_LINE_INDEX++);
			newSheetRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
			
			for(int columnIndex = 0; columnIndex< 3*columnSize -1; columnIndex++) {
				newSheetRow.createCell(columnIndex);//创建一整行
			}
			
			pageList.add(newSheetRow);
		}
		
		//创建完成新页之后，写院校代码
		String schoolCode = getSplitCode(schoolListCache.get(0), spilitSchoolCellIndex);
		String[] split = spilitSchoolCellIndex.split(",");
		String schoolName = schoolListCache.get(0).getCell(Integer.valueOf(split[split.length-1])+1).getStringCellValue().toString();
		
//		Row row = schoolListCache.get(0);
//		String schoolCode = row.getCell(0).getStringCellValue().toString()+"-" + row.getCell(2).getStringCellValue().toString();
		
		pageList.get(0).getCell(0).setCellValue("考点代码：" + schoolCode);
		pageList.get(0).getCell(3).setCellValue("考点名称：" + schoolName);
//		copyCell(schoolListCache.get(0).getCell(schoolCodeCellIndex), pageList.get(0).getCell(1));//学校编码第1列
//		copyCell(schoolListCache.get(0).getCell(schoolCodeCellIndex+1), pageList.get(0).getCell(2));//学校名称第3列
		if(!evenPage) {
			pageList.get(0).getCell(10).setCellValue("组别：____");
			pageList.get(0).getCell(12).setCellValue("签名：______");
		}
	}
	
	
	private static CellStyle createCommonStyle(SXSSFWorkbook workbook) {
		CellStyle newCellStyle = workbook.createCellStyle();
        newCellStyle.setBorderBottom(BorderStyle.THIN);
        newCellStyle.setBorderLeft(BorderStyle.THIN);
        newCellStyle.setBorderRight(BorderStyle.THIN);
        newCellStyle.setBorderTop(BorderStyle.THIN);
        return newCellStyle;
	}
	
	private static void copyCell(Cell sourceCell, Cell targetCell) {
		//处理单元格内容
		switch (sourceCell.getCellTypeEnum()) {
			case STRING:
				targetCell.setCellValue(sourceCell.getRichStringCellValue().getString());
				break;
			case NUMERIC:
				targetCell.setCellValue(sourceCell.getNumericCellValue());
				break;
			case BLANK:
				targetCell.setCellType(CellType.BLANK);
				break;
			case BOOLEAN:
				targetCell.setCellValue(sourceCell.getBooleanCellValue());
				break;
			case ERROR:
//				targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
//						targetCell.setCellErrorValue(FormulaError.VALUE);
				break;
			case FORMULA:
				targetCell.setCellFormula(sourceCell.getCellFormula());
				break;
			default:
				break;
		}
	}
	
	
	
	
}
