package com.opsmx.terraspin.artifact;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;
import com.opsmx.terraspin.util.ZipUtil;

public class S3Provider extends ArtifactProvider {

	private static final Logger log = LoggerFactory.getLogger(S3Provider.class);
	private static final String fileSeparator = File.separator;
	private static final String currentComponent = System.getenv("component");
	private static final ProcessUtil processutil = new ProcessUtil();
	private static final TerraAppUtil terraapputil = new TerraAppUtil();
	private static final ZipUtil ziputil = new ZipUtil();

	@Override
	public void envSetup(JSONObject artifactAccount) {

		log.info("started env setup of S3");
		String awsEnvUnhydratedCredentailsStr = "[default] \n" + "aws_access_key_id = AWSACCESSKEY \n"
				+ "aws_secret_access_key = AWSSECRETKEY";
		String awsEnvUnhydratedConfigStr = "[default]\n" + "region = REGION";

		String awsAccessKey = (String) artifactAccount.get("awsaccesskey");
		String awsSecretTKey = (String) artifactAccount.get("awssecretkey");
		String awsRegion = (String) artifactAccount.get("region");

		String awsEnvHydratedCredentailsStr = awsEnvUnhydratedCredentailsStr.replaceAll("AWSACCESSKEY", awsAccessKey)
				.replaceAll("AWSSECRETKEY", awsSecretTKey);

		String awsEnvHydratedConfigStr = awsEnvUnhydratedConfigStr.replaceAll("REGION", awsRegion);

		String currentUserDir = System.getProperty("user.home");

		File awsHomeDir = new File(currentUserDir + fileSeparator + ".aws");
		if (!awsHomeDir.exists())
			awsHomeDir.mkdir();

		File awsCredentailFileSource = new File(awsHomeDir.getPath() + fileSeparator + "credentials");
		terraapputil.overWriteStreamOnFile(awsCredentailFileSource,
				IOUtils.toInputStream(awsEnvHydratedCredentailsStr));

		File awsConfigFileSource = new File(awsHomeDir.getPath() + fileSeparator + "config");
		terraapputil.overWriteStreamOnFile(awsConfigFileSource, IOUtils.toInputStream(awsEnvHydratedConfigStr));

//		boolean ischangemod = processutil.runcommand("chmod 777 -R " + awsConfigFileSource.getAbsolutePath());
//		log.info("changing mod of file current file is " + awsConfigFileSource.getAbsolutePath()
//				+ " is file mod change " + ischangemod + " and process obj status " + processutil.getStatusRootObj());

		log.info("finish env setup of S3");
	}

	@Override
	public String getArtifactSourceReopName(String terraSpinStateRepoPath) {
		String spinStateRepoName = terraSpinStateRepoPath.split("/")[1];
		if (StringUtils.contains(spinStateRepoName, "/")) {
			spinStateRepoName = spinStateRepoName.split("/")[0];
			return spinStateRepoName;
		} else {
			return spinStateRepoName;
		}
	}

	@Override
	public String getOverrideFileNameWithPath(String tfVariableOverrideFileRepo) {
		// String VariableOverrideFilePath =
		// tfVariableOverrideFileRepo.trim().split("/")[0];
		String VariableOverrideFileName = tfVariableOverrideFileRepo
				.substring(tfVariableOverrideFileRepo.lastIndexOf("/")).replace("/","");
		return VariableOverrideFileName;
	}

	public String getArtifactSourceReopNameWithUsername(String terraSpinStateRepoPath) {
		// String spinStateRepoNameWithUserName =
		// terraSpinStateRepoPath.trim().split(".git")[0];
		String spinStateRepoNameWithUserName = terraSpinStateRepoPath;
		return spinStateRepoNameWithUserName;
	}

	@Override
	public boolean cloneOverrideFile(String cloneDir, String tfVariableOverrideFileReopNameWithUsername) {

		log.info("cloneOverrideFile form S3 bucket following bucket uri are -> "
				+ tfVariableOverrideFileReopNameWithUsername);
		String overrideFileBucketNameWithFileKeyName = tfVariableOverrideFileReopNameWithUsername.split("/", 2)[1];
		String overrideFileBucketName = overrideFileBucketNameWithFileKeyName.split("/", 2)[0];
		String overrideFileKeyName = overrideFileBucketNameWithFileKeyName.split("/", 2)[1];
		String overrideFileName = overrideFileKeyName.substring(overrideFileKeyName.lastIndexOf("/")).replace("/","");

		log.info("overrideFileBucketName : " + overrideFileBucketName);
		log.info("overrideFileKeyName : " + overrideFileKeyName);
		log.info("overrideFileName : " + overrideFileName);

		String OverrideVariableRepodir = cloneDir + fileSeparator + overrideFileBucketName;
		File OverrideVariableRepodirfile = new File(OverrideVariableRepodir);

		if (OverrideVariableRepodirfile.exists()) {
			try {
				FileUtils.forceDelete(OverrideVariableRepodirfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		boolean isOverrideFileCloned = downloadFileFormS3bucket(overrideFileBucketName, overrideFileKeyName,
				overrideFileName, cloneDir);

		if (isOverrideFileCloned) {
			log.info("is override variable file cloned from S3 " + isOverrideFileCloned + " and process obj status "
					+ processutil.getStatusRootObj());
			return isOverrideFileCloned;
		} else {
			log.info("error in cloning override variable file" + " and process obj status "
					+ processutil.getStatusRootObj());
			return isOverrideFileCloned;
		}
	}

	@Override
	public void pushStateArtifactSource(String currentUserDir, String spinStateRepoName, String staterepoDirPath,
			String uuId) {

		// String source2 =
		// "/home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";
		String source2 = currentUserDir + fileSeparator
				+ ".opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";

		File srcDir2 = new File(source2);
		String destination2 = staterepoDirPath;
		File destDir2 = new File(destination2);

		try {
			FileUtils.copyDirectoryToDirectory(srcDir2, destDir2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String zippath = staterepoDirPath + "/" + uuId + ".zip";
		String srczippath = staterepoDirPath + "/pipelineId-spinPipeId";

		File zippathfile = new File(zippath);
		zippathfile.delete();

		try {
			ziputil.zipDirectory(srczippath, zippath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			FileUtils.deleteDirectory(new File(srczippath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// log.info("cloneStateFile form S3 bucket following bucket uri are -> " +
		// spinStateRepoNameWithUserName);
		String stateFileBucketName = spinStateRepoName;
		String stateFileKeyName = uuId + ".zip";
		String stateFileName = uuId + ".zip";

		log.info("stateFileBucketName : " + stateFileBucketName);
		log.info("stateFileKeyName : " + stateFileKeyName);
		log.info("stateFileName : " + stateFileName);

		pushFileto3bucket(stateFileBucketName, stateFileKeyName, stateFileName, zippath);

	}

	@Override
	public boolean pullStateArtifactSource(String cloneDir, String spinStateRepoName,
			String spinStateRepoNameWithUserName, String uuId, String componentType) {

		log.info("cloneStateFile form S3 bucket following bucket uri are -> " + spinStateRepoNameWithUserName);
		String stateFileBucketName = spinStateRepoName.trim();
		String stateFileKeyName = uuId + ".zip";
		String stateFileName = uuId + ".zip";

		log.info("stateFileBucketName : " + stateFileBucketName);
		log.info("stateFileKeyName : " + stateFileKeyName);
		log.info("stateFileName : " + stateFileName);

		String OverrideVariableRepodir = cloneDir + fileSeparator + stateFileBucketName;
		File OverrideVariableRepodirfile = new File(OverrideVariableRepodir);

		if (OverrideVariableRepodirfile.exists()) {
			try {
				FileUtils.forceDelete(OverrideVariableRepodirfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		OverrideVariableRepodirfile.mkdirs();
		boolean isStateFileCloned;

		if (StringUtils.equalsAnyIgnoreCase("plan", componentType)) {
			isStateFileCloned = checkS3bucketPresent(stateFileBucketName);
			if (isStateFileCloned) {
				return isStateFileCloned;
			} else {
				log.info("error in finding of tf state s3 buscket");
				return isStateFileCloned;
			}
		}
		else {
			isStateFileCloned = downloadFileFormS3bucket(stateFileBucketName, stateFileKeyName, stateFileName,
					cloneDir);

			if (isStateFileCloned) {

				String changefilecommand = "chmod 777 -R " + cloneDir + fileSeparator + spinStateRepoName;
				boolean ischangemod = processutil.runcommand(changefilecommand);
				log.info("changing file mod command " + changefilecommand + " is command got success " + ischangemod
						+ " and process obj status " + processutil.getStatusRootObj());

				log.info("is tf state file cloned from S3 " + isStateFileCloned + " and process obj status "
						+ processutil.getStatusRootObj());
				return isStateFileCloned;
			} else {
				log.info("error in cloning tf state file file" + " and process obj status "
						+ processutil.getStatusRootObj());
				return isStateFileCloned;
			}

		}
		
	}

	public boolean checkS3bucketPresent(String bucketName) {

		AmazonS3 s3client = initializeS3Obj();
		boolean result;
		
		try {
			return s3client.doesBucketExistV2(bucketName);

		} catch (AmazonServiceException e) {
			log.info("Error in finding s3 bucket given bucket : " +bucketName );
			e.printStackTrace();
			result = false;
			return result;
		} 
		
	}
	
	public boolean downloadFileFormS3bucket(String bucketName, String FileKeyName, String FileName,
			String downloadDir) {

		AmazonS3 s3client = initializeS3Obj();
		File localFile = new File(downloadDir + fileSeparator + bucketName + fileSeparator + FileName);
		boolean result;
		
		try {
			s3client.getObject(new GetObjectRequest(bucketName, FileKeyName), localFile);
			result = localFile.exists() && localFile.canRead();
			return true;

		} catch (AmazonServiceException e) {
			log.info("Error in finding s3 object given object : " + FileKeyName );
			e.printStackTrace();
			result = false;
			return result;
		} 
		
	}
	
	public boolean pushFileto3bucket(String bucketName, String FileKeyName, String FileName, String pushFile) {

		AmazonS3 s3client = initializeS3Obj();
		String fileName = pushFile;
		try {
			// Upload a file as a new object with ContentType and title specified.
			PutObjectRequest request = new PutObjectRequest(bucketName, FileKeyName, new File(fileName));
			ObjectMetadata metadata = new ObjectMetadata();
			// metadata.setContentType("application/zip");
			metadata.addUserMetadata("title", "someTitle");
			request.setMetadata(metadata);
			s3client.putObject(request);
			return true;

		} catch (AmazonServiceException e) {
			System.out.println("AmazonServiceException ---------");
			e.printStackTrace();
			return false;
		} 

	}

	public AmazonS3 initializeS3Obj() {

		AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials((AWSCredentialsProvider) new AWSStaticCredentialsProvider(credentials)).build();

		return s3client;
	}
}
