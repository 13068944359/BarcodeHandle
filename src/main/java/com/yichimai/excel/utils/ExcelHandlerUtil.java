package com.yichimai.excel.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

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
import org.springframework.web.multipart.MultipartFile;

/**
 * 2020.1.14 由于代码冗余过多，废弃
 * 另用ExcelHandlerUtil2
 */
public class ExcelHandlerUtil {
	private static int ROW_NUM_EVERY_PAGE_XLSX_PORTRAIT = 50;  //每一页的行数（竖）
	private static int ROW_NUM_EVERY_PAGE_XLSX_TRANSVERSE = 33;  //每一页的行数（横）
	private static float HEIGHT_PT =  (float)14.25;//每一行的高度
	
	
	public static void paging(int sheetIndex,int cellIndex,boolean landscape,
			int pageSize,MultipartFile uploadFile,String targetFilePath) throws Exception {
		
	    String fileName = uploadFile.getOriginalFilename();// 获取文件名
	    String suffixName = fileName.substring(fileName.lastIndexOf("."));// 获取后缀
		
	    Workbook wb = null;
	    if(".xls".equals(suffixName)){
        	handleXls(sheetIndex,cellIndex,landscape,pageSize,uploadFile,targetFilePath);
        }else if(".xlsx".equals(suffixName)){
        	handleXlsx(sheetIndex,cellIndex,landscape,pageSize,uploadFile,targetFilePath);
        }else{
            throw new Exception("file type error");
        }
	}
	
	
	
	
	private static void handleXls(int sheetIndex,int cellIndex,boolean landscape,
			int pageSize,MultipartFile uploadFile,String targetFilePath) {
		try {
//			SXSSFWorkbook  // 用于处理大数据量以及超大数据量的导出
			SXSSFWorkbook newWorkbook=new SXSSFWorkbook();//创建Excel文件薄
	        SXSSFSheet newSheet=newWorkbook.createSheet();//创建工作表sheeet
	        int newSheetLineIndex = 0;
			
	        CellStyle newCellStyle = newWorkbook.createCellStyle();
	        newCellStyle.setBorderBottom(BorderStyle.THIN);
	        newCellStyle.setBorderLeft(BorderStyle.THIN);
	        newCellStyle.setBorderRight(BorderStyle.THIN);
	        newCellStyle.setBorderTop(BorderStyle.THIN);
	        
	        PrintSetup ps = newSheet.getPrintSetup();
	        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
	        ps.setLandscape(landscape); // 打印方向，true：横向，false：纵向
//	        int pageSize = landscape ? ROW_NUM_EVERY_PAGE_XLSX_TRANSVERSE : ROW_NUM_EVERY_PAGE_XLSX_PORTRAIT;
	        newSheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));//设置页眉为表格头
			
	        Footer newFooter = newSheet.getFooter();
	        newFooter.setCenter(HSSFFooter.page()+" / "+HSSFFooter.numPages());
	        
	        HSSFWorkbook workbook = new HSSFWorkbook(uploadFile.getInputStream());
        	HSSFSheet sheet = workbook.getSheetAt(sheetIndex);//获取sheet对象 
        	
        	HSSFRow row = sheet.getRow(2);
        	
        	Iterator<Cell> cellIterator = row.cellIterator();
        	while(cellIterator.hasNext()) {
        		Cell next = cellIterator.next();
        	}
        	
        	String diffFlag = sheet.getRow(1).getCell(cellIndex).getStringCellValue();//考点代码（用来判断是否变化）
        	//因为第0行是标题头，所以从第一行开始
        	
        	/**
        	 * 标题特殊处理，因为每一页打印都插入表头，所以会影响计算，因此单独拎出来处理
        	 */
        	HSSFRow titleRow=sheet.getRow(0);
        	//复制表头
        	SXSSFRow newTitleRow = newSheet.createRow(newSheetLineIndex);
    		newTitleRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
    		copyRow(newTitleRow, titleRow, newCellStyle);
        	
    		 // 返回该页的总行数
        	int lastRowNum=sheet.getLastRowNum();
            for (int lineIndex = 1; lineIndex <= lastRowNum; lineIndex++) {  //ineIndex <= lastRowNum 是因为下标+1了，不补1，会缺少最后一行
            	HSSFRow currentRow=sheet.getRow(lineIndex);
            	
            	String tmpFlag = currentRow.getCell(cellIndex).getStringCellValue();
            	
            	if(!diffFlag.equals(tmpFlag) && lineIndex>0  &&  (newSheetLineIndex % (pageSize * 2) != 0) ) {
            		/**
            		 * 该换页了，因此需要补齐空行
            		 */
            		if((newSheetLineIndex / pageSize + 1) % 2 == 1) {
            			 /**
            			  * 奇数页
            			  */
            			int lineShouldAdd = pageSize - (newSheetLineIndex % pageSize) + pageSize ;
//	            			newSheetLineIndex += lineShouldAdd;//需要插入空行
            			for(int i=0; i<lineShouldAdd ; i++) {
            				SXSSFRow newSheetRow = newSheet.createRow(newSheetLineIndex +1 );//为了消除第一行表头影响，需要在创建的时候单独+1
                    		newSheetRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
            				newSheetLineIndex ++;
            			}
            			System.out.println("newSheetLineIndex=" + newSheetLineIndex);
            		}else {
            			/**
            			 * 偶数页
            			 */
            			int lineShouldAdd = pageSize - (newSheetLineIndex % pageSize) ;
//	            			newSheetLineIndex += lineShouldAdd;//需要插入空行
            			for(int i=0; i<lineShouldAdd ; i++) {
            				SXSSFRow newSheetRow = newSheet.createRow(newSheetLineIndex +1 );//为了消除第一行表头影响，需要在创建的时候单独+1
                    		newSheetRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
            				newSheetLineIndex ++;
            			}
            			System.out.println("newSheetLineIndex=" + newSheetLineIndex);
            		}
            	}
        		diffFlag = tmpFlag;//更新 标志位
            	
            	//遍历搬运一行记录
        		SXSSFRow newSheetRow = newSheet.createRow(newSheetLineIndex +1 );//为了消除第一行表头影响，需要在创建的时候单独+1
        		newSheetRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
        		copyRow(newSheetRow, currentRow,newCellStyle);
        		newSheetLineIndex ++;//新表下标+1
            }
            
            FileOutputStream fileOut = new FileOutputStream(targetFilePath);
            newWorkbook.write(fileOut);
            newWorkbook.close();
            fileOut.close();
            
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	


	private static SXSSFWorkbook handleXlsx(int sheetIndex,int cellIndex,boolean landscape,
			int pageSize,MultipartFile uploadFile,String targetFilePath) {
		
		try { 
//			SXSSFWorkbook  // 用于处理大数据量以及超大数据量的导出
			SXSSFWorkbook newWorkbook=new SXSSFWorkbook();//创建Excel文件薄
	        SXSSFSheet newSheet=newWorkbook.createSheet();//创建工作表sheeet
	        int newSheetLineIndex = 0;
	        
	        CellStyle newCellStyle = newWorkbook.createCellStyle();
	        newCellStyle.setBorderBottom(BorderStyle.THIN);
	        newCellStyle.setBorderLeft(BorderStyle.THIN);
	        newCellStyle.setBorderRight(BorderStyle.THIN);
	        newCellStyle.setBorderTop(BorderStyle.THIN);
	        
	        PrintSetup ps = newSheet.getPrintSetup();
	        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
	        ps.setLandscape(landscape); // 打印方向，true：横向，false：纵向
//	        int pageSize = landscape ? ROW_NUM_EVERY_PAGE_XLSX_TRANSVERSE : ROW_NUM_EVERY_PAGE_XLSX_PORTRAIT;
	        newSheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));//设置页眉为表格头
	        
	        Footer newFooter = newSheet.getFooter();
	        newFooter.setCenter(HSSFFooter.page()+" / "+HSSFFooter.numPages());
	        
            SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(uploadFile.getInputStream()));
            XSSFWorkbook xssfWorkbook = workbook.getXSSFWorkbook();
        	XSSFSheet sheet = xssfWorkbook.getSheetAt(sheetIndex);//获取sheet对象 
        	
        	XSSFRow row = sheet.getRow(2);
        	
        	Iterator<Cell> cellIterator = row.cellIterator();
        	while(cellIterator.hasNext()) {
        		Cell next = cellIterator.next();
        	}
        	
        	String diffFlag = sheet.getRow(1).getCell(cellIndex).getStringCellValue();//考点代码（用来判断是否变化）
        	//因为第0行是标题头，所以从第一行开始
        	
        	/**
        	 * 标题特殊处理，因为每一页打印都插入表头，所以会影响计算，因此单独拎出来处理
        	 */
        	XSSFRow titleRow=sheet.getRow(0);
        	//复制表头
        	SXSSFRow newTitleRow = newSheet.createRow(newSheetLineIndex);
    		newTitleRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
    		copyRow(newTitleRow, titleRow, newCellStyle);
    		
            // 返回该页的总行数
        	int lastRowNum=sheet.getLastRowNum();
            for (int lineIndex = 1; lineIndex <= lastRowNum; lineIndex++) {  //ineIndex <= lastRowNum 是因为下标+1了，不补1，会缺少最后一行
            	XSSFRow currentRow=sheet.getRow(lineIndex);
            	
            	String tmpFlag = currentRow.getCell(cellIndex).getStringCellValue();
            	
            	if(!diffFlag.equals(tmpFlag) && lineIndex>0  &&  (newSheetLineIndex % (pageSize * 2) != 0) ) {
            		/**
            		 * 该换页了，因此需要补齐空行
            		 */
            		if((newSheetLineIndex / pageSize + 1) % 2 == 1) {
            			 /**
            			  * 奇数页
            			  */
            			int lineShouldAdd = pageSize - (newSheetLineIndex % pageSize) + pageSize ;
//	            			newSheetLineIndex += lineShouldAdd;//需要插入空行
            			for(int i=0; i<lineShouldAdd ; i++) {
            				SXSSFRow newSheetRow = newSheet.createRow(newSheetLineIndex +1 );//为了消除第一行表头影响，需要在创建的时候单独+1
                    		newSheetRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
            				newSheetLineIndex ++;
            			}
            			System.out.println("newSheetLineIndex=" + newSheetLineIndex);
            		}else {
            			/**
            			 * 偶数页
            			 */
            			int lineShouldAdd = pageSize - (newSheetLineIndex % pageSize) ;
//	            			newSheetLineIndex += lineShouldAdd;//需要插入空行
            			for(int i=0; i<lineShouldAdd ; i++) {
            				SXSSFRow newSheetRow = newSheet.createRow(newSheetLineIndex +1 );//为了消除第一行表头影响，需要在创建的时候单独+1
                    		newSheetRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
            				newSheetLineIndex ++;
            			}
            			System.out.println("newSheetLineIndex=" + newSheetLineIndex);
            		}
            	}
        		diffFlag = tmpFlag;//更新 标志位
            	
            	//遍历搬运一行记录
        		SXSSFRow newSheetRow = newSheet.createRow(newSheetLineIndex +1 );//为了消除第一行表头影响，需要在创建的时候单独+1
        		newSheetRow.setHeightInPoints(HEIGHT_PT);// 设置行高 默认14.25磅        
        		copyRow(newSheetRow, currentRow,newCellStyle);
        		newSheetLineIndex ++;//新表下标+1
            }
            
            FileOutputStream fileOut = new FileOutputStream(targetFilePath);
            newWorkbook.write(fileOut);
            newWorkbook.close();
            fileOut.close();
            
            return newWorkbook;
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
		return null;
	}
	
	

	private static void copyRow(SXSSFRow targetRow, HSSFRow sourceRow, CellStyle newCellStyle) {
		for (int i = sourceRow.getFirstCellNum(); i <= sourceRow.getLastCellNum(); i++) {
			HSSFCell sourceCell = sourceRow.getCell(i);
			SXSSFCell targetCell = targetRow.getCell(i);
			
			if (sourceCell != null) {
				if (targetCell == null) {
					targetCell = targetRow.createCell(i);
				}
				//拷贝单元格，包括内容和样式
				copyCell(targetCell, sourceCell);
				targetCell.setCellStyle(newCellStyle);
			}
		}
		
	}
	

	


	public static void copyRow(SXSSFRow targetRow, XSSFRow sourceRow,CellStyle newCellStyle) throws Exception {
		for (int i = sourceRow.getFirstCellNum(); i <= sourceRow.getLastCellNum(); i++) {
			XSSFCell sourceCell = sourceRow.getCell(i);
			SXSSFCell targetCell = targetRow.getCell(i);
			
			if (sourceCell != null) {
				if (targetCell == null) {
					targetCell = targetRow.createCell(i);
				}
				//拷贝单元格，包括内容和样式
				copyCell(targetCell, sourceCell);
				targetCell.setCellStyle(newCellStyle);
			}
		}
	}

	private static void copyCell(SXSSFCell targetCell, HSSFCell sourceCell) {
		//处理单元格内容
		switch (sourceCell.getCellTypeEnum()) {
			case STRING:
				targetCell.setCellValue(sourceCell.getRichStringCellValue());
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
	
	public static void copyCell(SXSSFCell targetCell, XSSFCell sourceCell) {
		//处理单元格内容
		switch (sourceCell.getCellTypeEnum()) {
			case STRING:
				targetCell.setCellValue(sourceCell.getRichStringCellValue());
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
	//			targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
//				targetCell.setCellErrorValue(FormulaError.VALUE);
				break;
			case FORMULA:
				targetCell.setCellFormula(sourceCell.getCellFormula());
				break;
			default:
				break;
		}
	}
	
}
