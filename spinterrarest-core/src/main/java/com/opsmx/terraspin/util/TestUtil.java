package com.opsmx.terraspin.util;

import java.io.File;

public class TestUtil {
	private static final String fileSeparator = File.separator;

	void timec() {
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestUtil tu = new TestUtil();
		
		//Test1
		/*
		 * String MYREGION = "abregion"; String MYACCESSKEY = "abaccess" ; String
		 * MYSECRETKEY = "absecret"; String providerConfig = "provider \"aws\"" +
		 * "{ region = \"" + MYREGION + "\"" + "\n access_key = \"" + MYACCESSKEY + "\""
		 * + "\n secret_key = \"" + MYSECRETKEY + "\"  }";
		 * 
		 * System.out.println("hey :" + providerConfig);
		 */

		//Test2
		/*
		 * String currentUserDir = System.getProperty("user.home");
		 * System.out.println("---" + currentUserDir);
		 * 
		 * ProcessUtil processutil = new ProcessUtil(); String gitclonecommand =
		 * "git clone https://lalitv92:Viui59skranmnmnntewm92!@github.com/lalitv92/trytest.git"
		 * ; String dir = "/home/opsmx/lalit/work/opsmx/extrathing/gittest/test2";
		 * 
		 * boolean isgitcloned = processutil.runcommandwithindir(gitclonecommand,
		 * "/home/opsmx"); // boolean isgitcloned =
		 * processutil.runcommand(gitclonecommand); System.out.println("-----" +
		 * processutil.getStatusRootObj().toString());
		 */
		
		//Test3
		String currentUserDir = System.getProperty("user.home");
		System.out.println("---" + currentUserDir);
		String extrapipelineidsrc = currentUserDir + fileSeparator + "extra/pipelineId-spinPipeId/ab";
		File extrapipelineidsrcdir = new File(extrapipelineidsrc);
		if (!extrapipelineidsrcdir.exists())
			System.out.println(extrapipelineidsrcdir.mkdirs());

	}

}
