/*
 * Copyright OpsMx, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opsmx.terraspin.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileUtil {

	public static void main(String[] args) {

		String currentUserDir = System.getProperty("user.home");
		System.out.println("---" + currentUserDir);

		String source = "/home/opsmx/lalit/work/opsmx/extra";
		File srcDir = new File(source);

		String destination = "/home/opsmx/lalit/work/opsmx/extrathing/timepa/gittest.zip";
		File destDir = new File(destination);

		destDir.delete();
		// FileUtils.deleteDirectory(destDir);
		System.out.println("done!!");

	}

}
