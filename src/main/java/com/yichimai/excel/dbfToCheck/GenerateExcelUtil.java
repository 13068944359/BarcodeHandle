package com.yichimai.excel.dbfToCheck;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;


/**
 * 统一纵向打印
 * 统一三大列，5小列
 * @author Lenovo
 *
 */
public class GenerateExcelUtil {

	private SXSSFWorkbook workbook;
	private SXSSFSheet sheet;
	private CellStyle cellStyle;
	private int pageSize;
	private String targetFilePath;
	private String encodeType;
	List<String> logList;
	
	public GenerateExcelUtil(String encodeType,List<String> logList) {
		super();
		this.encodeType = encodeType;
		this.logList = logList;
	}

	private int columnInColumnSize = 5; //在新版式里面的一个大列里面有多少小列
	private int columnSize = 3; //在新版式里面有 多少个大列
	private int excelLineIndex = 0;//记录新excel写到第几行了（绝对值
	
	//往目标excel写入数据
	public void writeSchoolInfoIntoExcel(List<String> targetStrList,String schoolCode,String schoolName) {

		List<SXSSFRow> pageList = new LinkedList<>();
		int rowIndexOfPageList = 1; //记录已经写到第几行 0开始（由于第一行默认是院校代码，所以从1开始）
		int columnIndexOfPageList = 0;//记录已经写到第几列 0开始
		boolean evenPage = false;//判断是否偶数页码？不是就需要补充一页空白
		
		createNewPage(pageList, evenPage, schoolCode, schoolName);
		for(String targetStr : targetStrList) {
			if(targetStr.length() == 0) {
				//空白行
			}else {
				String[] split = targetStr.split("-");
				for(int index = 0;index < split.length; index++) {
					pageList.get(rowIndexOfPageList).getCell(columnIndexOfPageList*columnInColumnSize + index).setCellValue(split[index]);
					pageList.get(rowIndexOfPageList).getCell(columnIndexOfPageList*columnInColumnSize + index).setCellStyle(cellStyle);
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
					createNewPage(pageList, evenPage, schoolCode, schoolName);
				}
			}
		}
		if(!evenPage) {//不是偶数页 需要手动补充新的一页
			evenPage = !evenPage;//标记是否偶数页
			createNewPage(pageList, evenPage, schoolCode, schoolName);
		}
	}
	
	
	private void createNewPage(	List<SXSSFRow> pageList,  boolean evenPage,
			String schoolCode,String schoolName) {
		pageList.clear();

		for(int count = 0;count < pageSize; count++) {  //先创建一整页空行
			SXSSFRow newSheetRow = sheet.createRow(excelLineIndex++);
			newSheetRow.setHeightInPoints((float) 14.25);// 设置行高 默认14.25磅        
			
			for(int columnIndex = 0; columnIndex< 3*columnInColumnSize -1; columnIndex++) {
				newSheetRow.createCell(columnIndex);//创建一整行
			}
			
			pageList.add(newSheetRow);
		}
		pageList.get(0);
		pageList.get(0).getCell(0);
		
		
		pageList.get(0).getCell(0).setCellValue("考点代码：" + schoolCode);
		pageList.get(0).getCell(3).setCellValue("考点名称：" + schoolName);
		if(!evenPage) {
			pageList.get(0).getCell(10).setCellValue("组别：____");
			pageList.get(0).getCell(12).setCellValue("签名：______");
		}
	}
	
	public void flush() throws Exception {
		FileOutputStream fileOut = new FileOutputStream(targetFilePath);
        workbook.write(fileOut);
        workbook.close();
        fileOut.close();
	}
	
	public void initExcel(String pageTitle,int pageSize,String targetFilePath) {
		this.targetFilePath = targetFilePath;
		this.pageSize = pageSize;
//	    SXSSFWorkbook  // 用于处理大数据量以及超大数据量的导出
		workbook=new SXSSFWorkbook();//创建Excel文件薄
        sheet=workbook.createSheet();//创建工作表sheeet
        cellStyle = createCommonStyle(workbook);//create common cell style
        PrintSetup ps = sheet.getPrintSetup();
        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
        ps.setLandscape(false); // 打印方向，true：横向，false：纵向    4.0版本默认纵向
		
        if(pageTitle != null) {
        	Header header = sheet.getHeader();
        	header.setCenter(pageTitle);
        }
        
        Footer newFooter = sheet.getFooter();
        newFooter.setCenter(HSSFFooter.page()+" / "+HSSFFooter.numPages());
        newFooter.setLeft("禁止先勾后检查，禁止连续勾几行");
        newFooter.setRight("技术支持：服务中心-萧家洲");
        
        //设置好列宽
		sheet.setDefaultColumnWidth(7 * 256);// 设置7个字符的宽度（7个数字）
		sheet.setColumnWidth(columnInColumnSize * 0 + 0, 5 * 256);// 第一列
		sheet.setColumnWidth(columnInColumnSize * 1 + 0, 5 * 256);
		sheet.setColumnWidth(columnInColumnSize * 2 + 0, 5 * 256);

		sheet.setColumnWidth(columnInColumnSize * 0 + 3, 5 * 256);// “检查”列
		sheet.setColumnWidth(columnInColumnSize * 1 + 3, 5 * 256);
		sheet.setColumnWidth(columnInColumnSize * 2 + 3, 5 * 256);

		sheet.setColumnWidth(columnInColumnSize * 0 + 4, 2 * 256);// 空白列
		sheet.setColumnWidth(columnInColumnSize*1 + 4, 2*256);
	}
	

	private CellStyle createCommonStyle(SXSSFWorkbook workbook) {
		CellStyle newCellStyle = workbook.createCellStyle();
        newCellStyle.setBorderBottom(BorderStyle.THIN);
        newCellStyle.setBorderLeft(BorderStyle.THIN);
        newCellStyle.setBorderRight(BorderStyle.THIN);
        newCellStyle.setBorderTop(BorderStyle.THIN);
        return newCellStyle;
	}
	
	
	public String getValue(Object o) throws Exception {
//		return o==null?"":new String(o.toString ().trim().getBytes ("gb2312"),"GBK");
//		return o==null?"":new String(o.toString ().trim().getBytes ("GBK"),"gb2312");
		switch (encodeType) {
		case "default":
			return o==null?"":o.toString ().trim();
		case "a":
			return o==null?"":new String(o.toString ().trim().getBytes ("iso-8859-1"),"GBK");
		default:
			throw new Exception("encodeType error = " + encodeType);
		}
		
//		return o==null?"":new String(o.toString ().trim().getBytes ("iso-8859-1"),"UTF-8");
//		return o==null?"":new String(o.toString ().trim().getBytes ("iso-8859-1"),"GBK");
	}
	
	public void generateEncodeTestLog(Object[] rowValues) throws Exception {
		//默认格式
		logList.add("<span>【编码格式预览】【默认】编码文本效果：</span>");
		for(Object o : rowValues) {
			logList.add(o==null?"":o.toString ().trim());
		}
		
		//iso-8859-1 ~ GBK
		logList.add("<span>【编码格式预览】【iso-8859-1 ~ GBK】编码文本效果：</span>");
		for(Object o : rowValues) {
			logList.add( o==null?"":new String(o.toString().trim().getBytes("iso-8859-1"),"GBK"));
		}
	}
	
	
	// 科目按照序号排序  key都是能从字符串转int类型
	public static List<String> getOrderedListByMapKeys(Map subjectMap){
		List<String> subjectList = new LinkedList<>();
		
		Set<String> keySet = subjectMap.keySet();
		subjectList.addAll(keySet);
		subjectList.sort((a,b) -> {
//			return Integer.parseInt(a)< Integer.parseInt(b)? -1 : 1; // order it from small to big
			return compairStrToInt(a,b); // 因为“教师资格”特殊，因此需要增加特殊判断逻辑
		});
		
		return subjectList;
	}
	
	//正常比较都是数字进行比较，但是“教师资格考试”比较特殊，科目里面带有字母"A"，需要专门针对这一特例进行特殊处理
	private static int compairStrToInt(String a,String b) {
		String aBeforeParse = a;
		boolean aFlag = false;
		if(a.contains("A")) {
			aBeforeParse = a.substring(0,a.lastIndexOf("A"));aFlag=true;
		}
		
		String bBeforeParse = b;
		boolean bFlag = false;
		if(b.contains("A")) {
			bBeforeParse = b.substring(0,b.lastIndexOf("A"));bFlag=true;
		}
		
		int aInt = Integer.parseInt(aBeforeParse);
		int bInt = Integer.parseInt(bBeforeParse);
		if(aInt != bInt) {
			return aInt - bInt;
		}else if( aFlag != bFlag) {
			return aFlag?1:-1;
		}else {
			return 0;
		}
	}
}
