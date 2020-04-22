package com.opsmx.terraspin.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileUtil {

	public static void main(String[] args) {

		String currentUserDir = System.getProperty("user.home");
		/*
		 * System.out.println("---" + currentUserDir);
		 * 
		 * String source = "/home/opsmx/lalit/work/opsmx/extra"; File srcDir = new
		 * File(source);
		 * 
		 * String destination =
		 * "/home/opsmx/lalit/work/opsmx/extrathing/timepa/gittest.zip"; File destDir =
		 * new File(destination);
		 * 
		 * destDir.delete(); // FileUtils.deleteDirectory(destDir);
		 * System.out.println("done!!");
		 */
		
		File currentTerraformInfraCodeDir = new File("/home/opsmx/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId");
		try {
			FileUtils.cleanDirectory(currentTerraformInfraCodeDir);
			System.out.println("done!!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
