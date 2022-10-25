package com.yichimai.excel.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

/**
 * 亮亮有需要采集学生信息整理成EXCEL的需求
 * 需要填充文本信息和图片到EXCEL当中
 * 人工处理需要大量的缩放图片操作，效率较低，因而研发此工具
 * 参考连接：https://blog.csdn.net/q15976405716/article/details/114269628
 */
public class GenerateExcelWitchPic {

	public static void generate() throws Exception {
		OutputStream ops = new FileOutputStream("D://pic.xlsx");
		
		SXSSFWorkbook workbook = new SXSSFWorkbook();//创建Excel文件薄
//		SXSSFSheet sheet = workbook.createSheet();//创建工作表sheeet
		insertPic(workbook);
		
		workbook.write(ops);
		workbook.close();
	}
	
	private static void insertPic(SXSSFWorkbook workbook) throws Exception {
		SXSSFSheet sheet = workbook.createSheet();

//		sheet.setDefaultColumnWidth(300);//先设置列宽再设置行高，不然无效
		sheet.setDefaultRowHeight((short)2000);//默认行高

		
		
		SXSSFDrawing sd = sheet.createDrawingPatriarch();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
//		XSSFClientAnchor anchor = new XSSFClientAnchor(0,0,2550,2550,0,0,1,1);
		XSSFClientAnchor anchor = new XSSFClientAnchor(0,0,0,0,1,1,2,2);
		anchor.setAnchorType(AnchorType.DONT_MOVE_DO_RESIZE);
//		ClassPathResource cpr = new ClassPathResource("D://home//test.jpg");
		File f = new File("D://home//test.jpg");
		FileInputStream fis = new FileInputStream(new File("D://home//test.jpg"));
		
		
		//图片读取到bufferImage
		BufferedImage image = ImageIO.read(fis);
		//图片写入流
		ImageIO.write(image, "jpeg", baos);
		sd.createPicture(anchor, workbook.addPicture(baos.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
		
		

		sheet.setColumnWidth(0, 30*256);//30个字符
		sheet.setColumnWidth(1, 30*256);

	}
	
	
	
	
	public static void main(String[] args) throws Exception  {
		generate();
	}
	
}
