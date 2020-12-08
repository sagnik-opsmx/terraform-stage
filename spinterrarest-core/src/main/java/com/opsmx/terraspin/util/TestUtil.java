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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

public class TestUtil {
	private static final String fileSeparator = File.separator;

	void timec() {
	}

	public static void createFolder(String bucketName, String folderName, AmazonS3 client) {
		// create meta-data for your folder and set content-length to 0
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		// create empty content
		InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
		// create a PutObjectRequest passing the folder name suffixed by /
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName, emptyContent, metadata);
		// send request to S3 to create folder
		client.putObject(putObjectRequest);
	}

	public static void main(String[] args) {
		TestUtil tu = new TestUtil();

		// Test1
		/*
		 * String MYREGION = "abregion"; String MYACCESSKEY = "abaccess" ; String
		 * MYSECRETKEY = "absecret"; String providerConfig = "provider \"aws\"" +
		 * "{ region = \"" + MYREGION + "\"" + "\n access_key = \"" + MYACCESSKEY + "\""
		 * + "\n secret_key = \"" + MYSECRETKEY + "\"  }";
		 * 
		 * System.out.println("hey :" + providerConfig);
		 */

		// Test2
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

		// Test3
		/*
		 * String currentUserDir = System.getProperty("user.home");
		 * System.out.println("---" + currentUserDir); String extrapipelineidsrc =
		 * currentUserDir + fileSeparator + "extra/pipelineId-spinPipeId/ab"; File
		 * extrapipelineidsrcdir = new File(extrapipelineidsrc); if
		 * (!extrapipelineidsrcdir.exists())
		 * System.out.println(extrapipelineidsrcdir.mkdirs());
		 */

		////////////////////////////////////////////////////////////////

		// Test4

		try {

			// System.out.println("Done uploding on S3 bucket");
			AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
			// with region command
			// AmazonS3 s3client = AmazonS3ClientBuilder.standard().withRegion("us-west-2")
			// .withCredentials((AWSCredentialsProvider) new
			// AWSStaticCredentialsProvider(credentials)).build();

			AmazonS3 s3client = AmazonS3ClientBuilder.standard()
					.withCredentials((AWSCredentialsProvider) new AWSStaticCredentialsProvider(credentials)).build();
			// String fileName = "/home/opsmx/lalit/work/opsmx/extrathing/timeD.zip";

			// PutObjectResult putObjRestult = s3client.putObject(new
			// PutObjectRequest("terraform-module", "k8Namespace/timeF.zip", new
			// File(fileName)).withCannedAcl(CannedAccessControlList.PublicRead));

			// Upload a file as a new object with ContentType and title specified.
			// PutObjectRequest request = new PutObjectRequest("terraform-module",
			// "k8Namespace/timeF.zip",new File(fileName));
			// ObjectMetadata metadata = new ObjectMetadata();
			// metadata.setContentType("application/zip");
			// metadata.addUserMetadata("title", "someTitle");
			// request.setMetadata(metadata);
			// s3client.putObject(request);
			// System.out.println("Done uploding on S3 bucket");

			// File f = new File(fileName);
			// TransferManager xfer_mgr = TransferManagerBuilder.standard().build();
			// try {
			// Upload xfer = xfer_mgr.upload("terraform-module", "extrathing", f);
			// } catch (AmazonServiceException e) {
			// System.err.println(e.getErrorMessage());
			// System.exit(1);
			// }
			// xfer_mgr.shutdownNow();

			// putObjRestult.

			// s3client.deleteObject("terraform-module", "k8Namespace/timeD.zip");
			File localFile = new File("/home/opsmx/lalit/work/opsmx/extrathing/timeX.zip");
			s3client.getObject(new GetObjectRequest("terraform-module", "k8Namespace/timeX.zip"), localFile);

		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			System.out.println("AmazonServiceException ---------");

			e.printStackTrace();
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
		}

		System.out.println("Done with fetching file form S3 bucket");

		////////////////////////////////////////////////////////////////
		// Test5
		/*
		 * File directory = new File(
		 * "/home/opsmx/lalit/work/opsmx/terraformtest/test/.terraform/modules/terraModule"
		 * ); File[] subdirs = directory.listFiles((FileFilter)
		 * DirectoryFileFilter.DIRECTORY); for (File dir : subdirs) {
		 * System.out.println("Directory: " + dir.getName()); }
		 */

	}

}
