package com.opsmx.terraspin.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileUtil {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

		String currentUserDir = System.getProperty("user.home");
		System.out.println("---" + currentUserDir);
		
		/*
		 * String source = "/home/opsmx/lalit/work/opsmx/extrathing/gittest/test2"; File
		 * srcDir = new File(source); try { FileUtils.cleanDirectory(srcDir); } catch
		 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		
		
		  String source = "/home/opsmx/lalit/work/opsmx/extra";
		  File srcDir =  new File(source);
		  
		  String destination = "/home/opsmx/lalit/work/opsmx/extrathing/timepa"; 
		  File destDir = new File(destination);
		  
		  try { FileUtils.copyDirectory(srcDir, destDir);
		  System.out.println("done!!"); } catch (IOException e) { e.printStackTrace();
		  }
		 
	}

}
