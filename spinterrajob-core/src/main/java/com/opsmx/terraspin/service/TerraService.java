/*
 * Copyright 2019 OpsMX, Inc.
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

package com.opsmx.terraspin.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.component.ApplicationStartup;
import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;

@Component
public class TerraService {

	private static final Logger log = LoggerFactory.getLogger(TerraService.class);
	static final String fileSeparator = File.separator;

	ApplicationStartup ApplicationStartup = new ApplicationStartup();
	JSONParser parser = new JSONParser();
	/*
	 * @Autowired TerraAppUtil terraAppUtil1;
	 */
	TerraAppUtil terraAppUtil = new TerraAppUtil();
	ProcessUtil processutil = new ProcessUtil();

	static String userHomeDir = System.getProperty("user.home");
	static String DEMO_HTML = "<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title>Opsmx TerraApp</title> </head> <body bgcolor='#000000'> <pre style=\"color:white;\"> \"OPTION_SCPACE\" </pre> </body> </html>";
	String spinApplicationName = "spinApp";
	String spinPipelineName = "spinPipe";
	String spinpiPelineId = "spinPipeId";
	String spinPlan = System.getenv("plan");
	String spinGitAccount = System.getenv("gitAccount");
	String spincloudAccount = System.getenv("cloudAccount");
	String spinArtifactAccount = System.getenv("artifactAccount");
	String applicationName = "applicationName-" + spinApplicationName;
	String pipelineName = "pipelineName-" + spinPipelineName;
	String pipelineId = "pipelineId-" + spinpiPelineId;

	@SuppressWarnings("unchecked")
	public void planStart(JSONObject artifactconfigaccount, String variableOverrideFile, String artifactType) {

		log.info("plan starting ::");
		log.info("applicationName:" + applicationName);
		log.info("pipelineName:" + pipelineName);
		log.info("pipelineId:" + pipelineId);

		File currentTerraformInfraCodeDir = terraAppUtil.createDirForPipelineId(applicationName, pipelineName,
				pipelineId);

		String statusFilePath = currentTerraformInfraCodeDir + "/planStatus";
		File statusFile = new File(statusFilePath);
		statusFile.delete();
		JSONObject status = new JSONObject();
		status.put("status", "RUNNING");
		InputStream statusInputStream = new ByteArrayInputStream(status.toString().getBytes(StandardCharsets.UTF_8));
		terraAppUtil.writeStreamOnFile(statusFile, statusInputStream);

		terraServicePlanSetting(artifactconfigaccount, spinArtifactAccount, spinPlan, currentTerraformInfraCodeDir,
				artifactType);

		TerraformIntialInitThread terraInitialInitOperationCall = new TerraformIntialInitThread(
				currentTerraformInfraCodeDir);
		Thread trigger = new Thread(terraInitialInitOperationCall);
		trigger.start();
		try {
			trigger.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String tfModuledir = findModuleRootDirAtPlan(artifactType);

		File exacttfRootModuleFilePathdir = new File(tfModuledir);

		TerraformInitThread terraInitOperationCall = new TerraformInitThread(exacttfRootModuleFilePathdir);
		Thread trigger1 = new Thread(terraInitOperationCall);
		trigger1.start();
		try {
			trigger1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		boolean ischangemod = processutil.runcommand("chmod 777 -R " + exacttfRootModuleFilePathdir);
		log.info("changing mod of exact tfRootModuleFilePath dir current inferred dir : " + tfModuledir
				+ " status mod change :: " + ischangemod);

		TerraformPlanThread terraOperationCall = new TerraformPlanThread(exacttfRootModuleFilePathdir,
				currentTerraformInfraCodeDir, variableOverrideFile);
		Thread trigger2 = new Thread(terraOperationCall);
		trigger2.start();
		try {
			trigger2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public JSONObject planStatus(String baseURL) {
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/planStatus";
		String planOutputURL = baseURL + "/api/v1/terraform/planOutput/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;

		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = null;
		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("status");
			if (statusStr.equalsIgnoreCase("RUNNING")) {
				outputJsonObj.put("status", "RUNNING");
			} else {
				outputJsonObj.put("status", statusStr);
				outputJsonObj.put("planOutputURL", planOutputURL);
				log.info("terrafor plan output json :" + outputJsonObj);
			}

		} catch (Exception e) {
			log.info("Error : parse plan status");
			throw new RuntimeException("parse plan status error ", e);
		}

		return outputJsonObj;
	}

	public String planOutput(String applicationName, String pipelineName, String pipelineId, String baseURL) {
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/planStatus";

		JSONObject jsonObj = new JSONObject();
		String statusStr = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));

			statusStr = (String) jsonObj.get("output");

		} catch (Exception e) {
			log.info("Error : parse plan out put");
			throw new RuntimeException("parse plan output error ", e);
		}
		String strToR = DEMO_HTML.replace("OPTION_SCPACE", statusStr);
		log.debug("terraform plan out put :" + strToR);
		return strToR;
	}

	public void terraServicePlanSetting(JSONObject artifactconfigaccount, String artifactAccount, String spinPlan,
			File currentTerraformInfraCodeDir, String artifactType) {
		String terraformInfraCode = null;

		if (StringUtils.isNoneEmpty(artifactAccount)) {

			if (StringUtils.equalsAnyIgnoreCase(artifactType, "S3")) {
				String planConfig = new String("module \"terraModule\"{source = \"s3::https://REPONAME\"}");
				terraformInfraCode = planConfig.replaceAll("REPONAME", spinPlan);
			}
			if (StringUtils.equalsAnyIgnoreCase(artifactType, "Github")) {
				String planConfig = new String("module \"terraModule\"{source = \"git::https://github.com/REPONAME\"}");
				terraformInfraCode = planConfig.replaceAll("REPONAME", spinPlan);
			}

		} else {
			terraformInfraCode = spinPlan;
		}

		String infraCodePath = currentTerraformInfraCodeDir.getPath() + "/infraCode.tf";
		File infraCodfile = new File(infraCodePath);
		if (!infraCodfile.exists()) {
			try {
				infraCodfile.createNewFile();
			} catch (IOException e) {
				log.info("Error : terraform InfrCodfile Creation");
				throw new RuntimeException("Error : terraform InfrCodfile Creation ", e);

			}
		}

		InputStream infraCodeInputStream = new ByteArrayInputStream(
				terraformInfraCode.getBytes(StandardCharsets.UTF_8));
		terraAppUtil.overWriteStreamOnFile(infraCodfile, infraCodeInputStream);
	}

	@SuppressWarnings("unchecked")
	public String applyStart(String clonerepodir, String baseURL, String variableOverrideFile) {

		File currentTerraformInfraCodeDir = terraAppUtil.createDirForPipelineId(applicationName, pipelineName,
				pipelineId);

		File planPathDir = currentTerraformInfraCodeDir;

		String statusFilePath = planPathDir + "/applyStatus";
		File statusFile = new File(statusFilePath);
		statusFile.delete();
		JSONObject status = new JSONObject();
		status.put("status", "RUNNING");
		InputStream statusInputStream = new ByteArrayInputStream(status.toString().getBytes(StandardCharsets.UTF_8));

		terraAppUtil.writeStreamOnFile(statusFile, statusInputStream);

		String source = clonerepodir;
		File srcDir = new File(source);

		String destination = userHomeDir
				+ "/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";
		File destDir = new File(destination);

		try {
			FileUtils.copyDirectory(srcDir, destDir);
			System.out.println("done!!");
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean ischangemod = processutil.runcommand("chmod 777 -R " + destination);
		log.info("-----ischangemod status ----" + ischangemod);

		String tfModuledir = findModuleRootDir();
		File exacttfRootModuleFilePathdir = new File(tfModuledir);

		TerraformApplyThread terraOperationCall = new TerraformApplyThread(exacttfRootModuleFilePathdir, planPathDir,
				variableOverrideFile);
		Thread trigger = new Thread(terraOperationCall);
		trigger.start();

		try {
			trigger.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String statusPollURL = baseURL + "/api/v1/terraform/applyStatus/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;

		JSONObject outRootObj = new JSONObject();
		outRootObj.put("status", "RUNNING");
		outRootObj.put("statusurl", statusPollURL);
		log.info("terraform apply status :" + status);
		log.debug("terraform apply status url :" + statusPollURL);
		return outRootObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public JSONObject applyStatus(String baseURL) {

		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/applyStatus";

		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = new String();

		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("status");
			if (statusStr.equalsIgnoreCase("running")) {
				outputJsonObj.put("status", "RUNNING");
			} else {
				outputJsonObj.put("status", statusStr);

				String applyOutputURL = baseURL + "/api/v1/terraform/applyOutput/" + applicationName + "/"
						+ pipelineName + "/" + pipelineId;
				
				String tfModuledir = findModuleRootDir();
				File exacttfRootModuleFilePathdir = new File(tfModuledir);

				JSONObject planExeOutputValuesObject = terraServicePlanExeOutputValues(exacttfRootModuleFilePathdir);

				outputJsonObj.put("outputValues", planExeOutputValuesObject);
				outputJsonObj.put("applyOutputURL", applyOutputURL);
				log.info("terraform apply status :" + outputJsonObj);
			}

		} catch (Exception e) {
			log.info("Error : terraform apply status");
			throw new RuntimeException("terraform apply status error ", e);

		}
		return outputJsonObj;
	}

	public String applyOutput(String applicationName, String pipelineName, String pipelineId, String baseURL) {

		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/applyStatus";

		JSONObject jsonObj = new JSONObject();
		String statusStr = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("output");
		} catch (Exception e) {
			log.info("Error : terraform apply output");
			throw new RuntimeException("terraform apply output error ", e);
		}
		String strToR = DEMO_HTML.replaceAll("OPTION_SCPACE", statusStr);
		log.debug("terraform apply output :" + strToR);
		return strToR;
	}

	@SuppressWarnings("unchecked")
	public JSONObject terraServicePlanExeOutputValues(File terraformCodeDir) {

		JSONObject planExeOutputValuesJsonObj = new JSONObject();
		Process exec;
		try {

			String exactScriptPath = System.getProperty("user.home") + "/.opsmx/script/exeTerraformOutput.sh";
			exec = Runtime.getRuntime()
					.exec(new String[] { "/bin/sh", "-c", "sh " + exactScriptPath + " " + terraformCodeDir.getPath() });
			exec.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			String line = "";
			String tempLine = "";
			while ((tempLine = reader.readLine()) != null) {

				System.out.println("SPINNAKER PROPERTY whole line -> " + tempLine);
				String key = tempLine.split("=", 2)[0].trim();
				String value = tempLine.split("=", 2)[1].trim();
				planExeOutputValuesJsonObj.put(key, value);
				line = line + tempLine.trim() + System.lineSeparator();

				if (StringUtils.isEmpty(key)) {
					System.out.println("SPINNAKER_PROPERTY_" + "NoKey" + "=" + "NoValue");
				} else {
					System.out.println("SPINNAKER_PROPERTY_" + key + "=" + value);
				}
			}

			BufferedReader reader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line2 = "";
			String tempLine2 = "";
			while ((tempLine2 = reader2.readLine()) != null) {
				line2 = line2 + tempLine2.trim() + System.lineSeparator();
			}

			reader.close();
			reader2.close();
			if (!line2.isEmpty()) {
				log.info("Error : terraform Plan script output values :" + line2);
				System.out.println("SPINNAKER_PROPERTY_" + "NoKey" + "=" + "NoValue");
			}
		} catch (IOException | InterruptedException e) {
			log.info("Error : terraform Plan script OutputValues  ouput");
			throw new RuntimeException("Error : terraform Plan script OutputValues  ouput ", e);
		}
		log.debug("terraform plan output values :" + planExeOutputValuesJsonObj);
		return planExeOutputValuesJsonObj;
	}

	@SuppressWarnings("unchecked")
	public String destroyStart(String clonerepodir, String baseURL, String variableOverrideFile) {

		File currentTerraformInfraCodeDir = terraAppUtil.createDirForPipelineId(applicationName, pipelineName,
				pipelineId);

		File planPathDir = currentTerraformInfraCodeDir;

		String statusFilePath = planPathDir + "/destroyStatus";
		File statusFile = new File(statusFilePath);
		statusFile.delete();
		JSONObject status = new JSONObject();
		status.put("status", "RUNNING");
		InputStream statusInputStream = new ByteArrayInputStream(status.toString().getBytes(StandardCharsets.UTF_8));

		terraAppUtil.writeStreamOnFile(statusFile, statusInputStream);

		String source = clonerepodir;
		File srcDir = new File(source);

		String destination = userHomeDir
				+ "/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";
		File destDir = new File(destination);

		try {
			FileUtils.copyDirectory(srcDir, destDir);
			System.out.println("done!!");
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean ischangemod = processutil.runcommand("chmod 777 -R " + destination);
		log.info("-----ischangemod status ----" + ischangemod);

		String tfModuledir = findModuleRootDir();
		// String exacttfRootModuleFilePathinStr = currentTerraformInfraCodeDir + "/" +
		// tfModuledir;
		File exacttfRootModuleFilePathdir = new File(tfModuledir);

		TerraformDestroyThread terraOperationCall = new TerraformDestroyThread(exacttfRootModuleFilePathdir,
				planPathDir, variableOverrideFile);
		Thread trigger = new Thread(terraOperationCall);
		trigger.start();

		try {
			trigger.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String statusPollURL = baseURL + "/api/v1/terraform/destroyStatus/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;

		JSONObject outRootObj = new JSONObject();
		outRootObj.put("statusurl", statusPollURL);
		outRootObj.put("status", "RUNNING");
		log.info("terraform destroy status :" + status);
		log.debug("terrafor destroy status url :" + statusPollURL);
		return outRootObj.toJSONString();

	}

	@SuppressWarnings("unchecked")
	public JSONObject destroyStatus(String baseURL) {
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/destroyStatus";

		String destroyOutputURL = baseURL + "/api/v1/terraform/destroyOutput/" + applicationName + "/" + pipelineName
				+ "/" + pipelineId;

		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = null;
		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("status");
			if (statusStr.equalsIgnoreCase("RUNNING")) {
				outputJsonObj.put("status", "RUNNING");
			} else {
				outputJsonObj.put("status", statusStr);
				outputJsonObj.put("destroyOutputURL", destroyOutputURL);
				log.info("terraform destroy status :" + outputJsonObj);
			}

		} catch (Exception e) {
			log.info("Error : terraform destroy status");
			throw new RuntimeException("Error : terraform destroy status ", e);
		}

		return outputJsonObj;
	}

	public String destroyOutput(String applicationName, String pipelineName, String pipelineId, String baseURL) {

		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/destroyStatus";

		JSONObject jsonObj = new JSONObject();
		String statusStr = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));

			statusStr = (String) jsonObj.get("output");

		} catch (Exception e) {
			log.info("Error : terraform destroy ouput");
			throw new RuntimeException("Error : terraform destroy output ", e);
		}
		String strToR = DEMO_HTML.replaceAll("OPTION_SCPACE", statusStr);
		log.debug("terraform destroy output :" + strToR);
		return strToR;
	}
	
	public String findModuleRootDir() {

		String currentTerraformInfraCodeDir = userHomeDir
				+ "/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";

		String tfModulejsonpath = currentTerraformInfraCodeDir + "/.terraform/modules/modules.json";
		String tfModulejson = terraAppUtil.getStrJson(tfModulejsonpath);

		JSONObject moduleConfigObject = null;
		try {
			moduleConfigObject = (JSONObject) parser.parse(tfModulejson);
		} catch (ParseException pe) {
			log.info("Exception while parsing  tf module json :: " + tfModulejson);
			throw new RuntimeException("config Parse error:", pe);
		}

		JSONObject correcttModule = null;
		JSONArray Modules = (JSONArray) moduleConfigObject.get("Modules");
		for (int i = 0; i < Modules.size(); i++) {
			JSONObject currentModule = (JSONObject) Modules.get(i);
			String currentKey = (String) currentModule.get("Key");
			if (StringUtils.equalsAnyIgnoreCase("terraModule", currentKey)) {
				correcttModule = currentModule;
				break;
			}
		}

		String tfModuledir = currentTerraformInfraCodeDir + "/" + (String) correcttModule.get("Dir");
		return tfModuledir;
	}

	@SuppressWarnings("unchecked")
	public String findModuleRootDirAtPlan(String artifactType) {

		String tfModuledir = "";
		String currentTerraformInfraCodeDir = userHomeDir
				+ "/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";

		if (StringUtils.equalsIgnoreCase(artifactType, "Github")) {
			String tfModulejsonpath = currentTerraformInfraCodeDir + "/.terraform/modules/modules.json";
			String tfModulejson = terraAppUtil.getStrJson(tfModulejsonpath);

			JSONObject moduleConfigObject = null;
			try {
				moduleConfigObject = (JSONObject) parser.parse(tfModulejson);
			} catch (ParseException pe) {
				log.info("Exception while parsing  tf module json :: " + tfModulejson);
				throw new RuntimeException("config Parse error:", pe);
			}

			JSONObject correcttModule = null;
			JSONArray Modules = (JSONArray) moduleConfigObject.get("Modules");
			for (int i = 0; i < Modules.size(); i++) {
				JSONObject currentModule = (JSONObject) Modules.get(i);
				String currentKey = (String) currentModule.get("Key");
				if (StringUtils.equalsAnyIgnoreCase("terraModule", currentKey)) {
					correcttModule = currentModule;
					break;
				}
			}

			tfModuledir = currentTerraformInfraCodeDir + "/" + (String) correcttModule.get("Dir");

		}
		if (StringUtils.equalsIgnoreCase(artifactType, "S3")) {

			String tfModulejsonpath = currentTerraformInfraCodeDir + "/.terraform/modules/modules.json";
			String tfModulejson = terraAppUtil.getStrJson(tfModulejsonpath);

			JSONObject moduleConfigObject = new JSONObject();
			JSONObject modifiedModuleConfigObject = new JSONObject();
			try {
				moduleConfigObject = (JSONObject) parser.parse(tfModulejson);
			} catch (ParseException pe) {
				log.info("Exception while parsing  tf module json :: " + tfModulejson);
				throw new RuntimeException("config Parse error:", pe);
			}

			JSONObject correcttModule = null;
			JSONArray Modules = (JSONArray) moduleConfigObject.get("Modules");
			JSONArray ModifiedModules = new JSONArray();
			for (int i = 0; i < Modules.size(); i++) {
				JSONObject currentModule = (JSONObject) Modules.get(i);
				ModifiedModules.add(currentModule);
				String currentKey = (String) currentModule.get("Key");
				if (StringUtils.equalsAnyIgnoreCase("terraModule", currentKey)) {
					correcttModule = currentModule;
					break;
				}
			}

			tfModuledir = currentTerraformInfraCodeDir + "/" + (String) correcttModule.get("Dir");
			String tfFilesDirName = terraAppUtil.getFirstDirNameInGivenDir(tfModuledir);
			tfModuledir = tfModuledir + "/" + tfFilesDirName;

			String exactCorrecttModuleDir = (String) correcttModule.get("Dir") + "/" + tfFilesDirName;
			correcttModule.put("Dir", exactCorrecttModuleDir);
			ModifiedModules.add(correcttModule);
			modifiedModuleConfigObject.put("Modules", ModifiedModules);

			File tfModulejsonFilepath = new File(tfModulejsonpath);
			InputStream tfModifiedModulejsonInputStream = new ByteArrayInputStream(
					modifiedModuleConfigObject.toString().getBytes(StandardCharsets.UTF_8));
			terraAppUtil.overWriteStreamOnFile(tfModulejsonFilepath, tfModifiedModulejsonInputStream);

		}

		return tfModuledir;

	}

}
