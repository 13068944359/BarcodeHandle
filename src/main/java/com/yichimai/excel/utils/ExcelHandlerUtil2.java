package com.yichimai.excel.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
 *
 */
public class ExcelHandlerUtil2 {
	private static Logger LOGGER = LoggerFactory.getLogger(ExcelHandlerUtil2.class);
	private static float HEIGHT_PT =  (float)14.25;//每一行的高度
	private static int NEW_SHEET_LINE_INDEX = 0;
	
	public static void paging(int sheetIndex,int schoolCodeCellIndex,boolean landscape,
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
	    handle_generate_file(sheetIndex, schoolCodeCellIndex, landscape, pageSize, wb, uploadFile, targetFilePath);
	}
	
	/**
	 * 2.0 根据类型处理
	 */
	private static void handle_generate_file(int sheetIndex,int schoolCodeCellIndex,boolean landscape,
			int pageSize,Workbook wb,MultipartFile uploadFile,String targetFilePath) {
		try{
//			SXSSFWorkbook  // 用于处理大数据量以及超大数据量的导出
			SXSSFWorkbook newWorkbook=new SXSSFWorkbook();//创建Excel文件薄
	        SXSSFSheet newSheet=newWorkbook.createSheet();//创建工作表sheeet
	        
	        CellStyle newCellStyle = createCommonStyle(newWorkbook);//create common cell style
	        
	        PrintSetup ps = newSheet.getPrintSetup();
	        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
	        ps.setLandscape(landscape); // 打印方向，true：横向，false：纵向
//	        newSheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));//设置页眉为表格头 //2.0版本表格头要求显示对应的学校代码和名称，因此需要手动实现
			
	        Footer newFooter = newSheet.getFooter();
	        newFooter.setCenter(HSSFFooter.page()+" / "+HSSFFooter.numPages());
	        
	        handle_excute_rule( wb, sheetIndex, schoolCodeCellIndex, pageSize, newWorkbook, newSheet, newCellStyle);
	        
	        FileOutputStream fileOut = new FileOutputStream(targetFilePath);
            newWorkbook.write(fileOut);
            newWorkbook.close();
            fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
		}
		
	}
	
	private static void handle_excute_rule(Workbook wb,int sheetIndex,int schoolCodeCellIndex,int pageSize,
			SXSSFWorkbook newWorkbook,SXSSFSheet newSheet,CellStyle newCellStyle) throws Exception {
		int newSheetLineIndex = 0;
    	Sheet sheet = wb.getSheetAt(sheetIndex);//获取sheet对象 
    	String currentSchoolCode = sheet.getRow(1).getCell(schoolCodeCellIndex).getStringCellValue();//考点代码（用来判断是否变化）
		
    	List<Row> schoolListCache = new LinkedList<Row>();// 缓存一个学校的数据
    	 // 返回该页的总行数
    	int lastRowNum=sheet.getLastRowNum();
    	for (int lineIndex = 1; lineIndex <= lastRowNum; lineIndex++) {  //ineIndex <= lastRowNum 是因为下标从1开始而不是0，不补1，会缺少最后一行
    		Row currentRow=sheet.getRow(lineIndex);
        	
        	String schoolCode = currentRow.getCell(schoolCodeCellIndex).getStringCellValue();
        	/**
        	 * 循环读取，把一个考点的记录读取到一个list中
        	 * 读取到下一个考点，则说明已经读取完毕
        	 * 再处理下一个考点之前，先对已有记录的list进行处理，并清空
        	 */
        	if(currentSchoolCode.equals(schoolCode)) {
        		schoolListCache.add(currentRow);// add item to listCache
        	}else {
        		handle_school_jszgz(  sheetIndex, schoolCodeCellIndex, schoolListCache, pageSize, newSheet,newCellStyle);
        		
        		currentSchoolCode = schoolCode;
        		schoolListCache.clear();// 清空缓存
        		schoolListCache.add(currentRow);// add item to listCache
        	}
        }
		handle_school_jszgz(  sheetIndex, schoolCodeCellIndex, schoolListCache, pageSize, newSheet,newCellStyle);
	}
	
	private static final int COLUMN_SIZE_JSZG = 5;//教师资格在新版式里面的一个大列里面有多少小列
	
	//教师资格证 右侧列数：5
	//表头字段：省份代码	考区代码	考区名称	考点代码	考点名称
	//表格字段：科目代码	科目名称	考场号	起始姓名	结束姓名
	private static void handle_school_jszgz(int sheetIndex,int schoolCodeCellIndex,
			 List<Row> schoolListCache,int pageSize,
			SXSSFSheet newSheet,CellStyle newCellStyle) throws Exception {
		//设置好列宽
		newSheet.setDefaultColumnWidth(7*256);//设置7个字符的宽度（7个数字）
		newSheet.setColumnWidth(COLUMN_SIZE_JSZG*0 + 0, 5*256);//第一列
		newSheet.setColumnWidth(COLUMN_SIZE_JSZG*1 + 0, 5*256);
		newSheet.setColumnWidth(COLUMN_SIZE_JSZG*2 + 0, 5*256);

		newSheet.setColumnWidth(COLUMN_SIZE_JSZG*0 + 3, 5*256);//“检查”列
		newSheet.setColumnWidth(COLUMN_SIZE_JSZG*1 + 3, 5*256);
		newSheet.setColumnWidth(COLUMN_SIZE_JSZG*2 + 3, 5*256);

		newSheet.setColumnWidth(COLUMN_SIZE_JSZG*0 + 4, 2*256);//空白列
		newSheet.setColumnWidth(COLUMN_SIZE_JSZG*1 + 4, 2*256);
		
		/**
		 * 数据的预处理，先把数据加工好，再去填充新sheet
		 * 4种类型：科目、标题头、姓名、空行
		 */
		List<String> targetStrList = new LinkedList<>();
		String subjectCodeCache = null;//教师资格证科目代码的index是5，标志位判断科目是否发生变化
		StringBuilder sb = new StringBuilder();
		for(Row rowOfSchool : schoolListCache) {
			String currentSubjectCode = rowOfSchool.getCell(5).getStringCellValue();//科目代码的index是5
			if(!currentSubjectCode.equals(subjectCodeCache)) {
				subjectCodeCache = currentSubjectCode;
				//插入空行、科目行、标题行
				targetStrList.add("");
				targetStrList.add("科目-" + currentSubjectCode);
				targetStrList.add("考场-起始姓名-结束姓名-检查");
			}
			sb.setLength(0);// clear
			sb.append(rowOfSchool.getCell(7).getStringCellValue().toString());//考场号
			sb.append("-");
			sb.append(rowOfSchool.getCell(8).getStringCellValue().toString());//姓名
			sb.append("-");
			sb.append(rowOfSchool.getCell(9).getStringCellValue().toString());//姓名
			sb.append("- ");
			targetStrList.add(sb.toString());
		}
		
		targetStrList.remove(0);//去掉第一个空行
		
		
		List<SXSSFRow> pageList = new LinkedList<>();
		int rowIndexOfPageList = 1; //记录已经写到第几行 0开始（由于第一行默认是院校代码，所以从1开始）
		int columnIndexOfPageList = 0;//记录已经写到第几列 0开始
		boolean evenPage = false;//偶数页码？不是就需要补充一页空白

		createNewPage(pageSize, schoolCodeCellIndex, newSheet, pageList, schoolListCache, COLUMN_SIZE_JSZG);
		for(String targetStr : targetStrList) {
			if(targetStr.length() == 0) {
				//空白行
			}else {
				String[] split = targetStr.split("-");
				for(int index = 0;index < split.length; index++) {
					pageList.get(rowIndexOfPageList).getCell(columnIndexOfPageList*COLUMN_SIZE_JSZG + index).setCellValue(split[index]);
					pageList.get(rowIndexOfPageList).getCell(columnIndexOfPageList*COLUMN_SIZE_JSZG + index).setCellStyle(newCellStyle);
				}
			}
			
			rowIndexOfPageList ++;
			if(rowIndexOfPageList == pageSize) {
				rowIndexOfPageList = 1;//重新开始的1行
				columnIndexOfPageList++;
				if(columnIndexOfPageList == 3) {
					columnIndexOfPageList = 0;//重新开始的1页，从0列开始
					//创建新开始的1页
					createNewPage(pageSize, schoolCodeCellIndex, newSheet, pageList, schoolListCache, COLUMN_SIZE_JSZG);
					
					evenPage = !evenPage;//标记是否偶数页
				}
			}
		}
		if(!evenPage) {//不是偶数页
			createNewPage(pageSize, schoolCodeCellIndex, newSheet, pageList, schoolListCache, COLUMN_SIZE_JSZG);
		}
		
	}
	
	
	private static void createNewPage(int pageSize,int schoolCodeCellIndex,SXSSFSheet newSheet,
			List<SXSSFRow> pageList, List<Row> schoolListCache, int columnSize) {
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
		copyCell(schoolListCache.get(0).getCell(schoolCodeCellIndex), pageList.get(0).getCell(0));//学校编码第1列
		copyCell(schoolListCache.get(0).getCell(schoolCodeCellIndex+1), pageList.get(0).getCell(2));//学校名称第3列
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
