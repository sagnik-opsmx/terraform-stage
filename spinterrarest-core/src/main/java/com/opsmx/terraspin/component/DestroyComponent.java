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

package com.opsmx.terraspin.component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.service.TerraService;
import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;
import com.opsmx.terraspin.util.ZipUtil;

@Component
public class DestroyComponent {

	private static final String fileSeparator = File.separator;
	JSONParser parser = new JSONParser();
	TerraAppUtil terraapputil = new TerraAppUtil();
	ProcessUtil processutil = new ProcessUtil();
	TerraService terraservice = new TerraService();
	ZipUtil ziputil = new ZipUtil();

	String spinApplicationName = "spinApp";
	String spinPipelineName = "spinPipe";
	String spinpiPelineId = "spinPipeId";
	String applicationName = "applicationName-" + spinApplicationName;
	String pipelineName = "pipelineName-" + spinPipelineName;
	String pipelineId = "pipelineId-" + spinpiPelineId;

	@SuppressWarnings("unchecked")
	public String onTerraspinDestroy(String payload, String baseURL) {

		File currentTerraformInfraCodeDir = terraapputil.createDirForPipelineId(applicationName, pipelineName,
				pipelineId);

		writeDestroyStatus(currentTerraformInfraCodeDir, "RUNNING");

		DestroyComponentThread DCThread = new DestroyComponentThread(payload, currentTerraformInfraCodeDir);
		Thread thread1 = new Thread(DCThread);
		thread1.start();

		String statusPollURL = baseURL + "/api/v1/terraform/destroyStatus";

		JSONObject outRootObj = new JSONObject();
		outRootObj.put("status", "RUNNING");
		outRootObj.put("statusurl", statusPollURL);
		return outRootObj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	void writeDestroyStatus(File currentTerraformInfraCodeDir, String statusstr) {

		String statusFilePath = currentTerraformInfraCodeDir + "/destroyStatus";
		File statusFile = new File(statusFilePath);

		if (statusFile.length() == 0) {
			JSONObject status = new JSONObject();
			status.put("status", statusstr);

			InputStream statusInputStream = new ByteArrayInputStream(
					status.toString().getBytes(StandardCharsets.UTF_8));
			terraapputil.overWriteStreamOnFile(statusFile, statusInputStream);
		} else {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj = (JSONObject) parser.parse(new FileReader(statusFilePath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}

			jsonObj.put("status", statusstr);
			InputStream statusInputStream = new ByteArrayInputStream(
					jsonObj.toString().getBytes(StandardCharsets.UTF_8));
			terraapputil.overWriteStreamOnFile(statusFile, statusInputStream);
		}
	}

	JSONObject getDestroyStatus(File currentTerraformInfraCodeDir) {

		String statusFilePath = currentTerraformInfraCodeDir + "/destroyStatus";
		File statusFile = new File(statusFilePath);

		if (statusFile.length() == 0) {
			return null;
		} else {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj = (JSONObject) parser.parse(new FileReader(statusFilePath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return jsonObj;
		}
	}

	class DestroyComponentThread implements Runnable {

		private final Logger log = LoggerFactory.getLogger(DestroyComponentThread.class);
		public String payload;
		public File currentTerraformInfraCodeDir;

		public DestroyComponentThread(String payload, File currentTerraformInfraCodeDir) {
			this.payload = payload;
			this.currentTerraformInfraCodeDir = currentTerraformInfraCodeDir;
		}

		@Override
		public void run() {

			JSONObject payloadJsonObject = new JSONObject();

			try {
				payloadJsonObject = (JSONObject) parser.parse(payload);
			} catch (Exception e) {
				log.info(":: Exception while parsing payload ::" + payload);
				throw new RuntimeException("Payload parse error ", e);
			}

			String spinArtifactAccount = (String) payloadJsonObject.get("artifactAccount");
			String spinPlan = (String) payloadJsonObject.get("plan");
			String tfVariableOverrideFileRepo = (String) payloadJsonObject.get("variableOverrideFileRepo");
			String spinStateRepo = (String) payloadJsonObject.get("stateRepo");
			String uuId = (String) payloadJsonObject.get("uuId");

			log.info("System info current user -> " + System.getProperty("user.name") + " & current dir -> "
					+ System.getProperty("user.home"));
			log.info("Given terraform module path -> " + spinPlan);
			log.info("Given artifact account name -> " + spinArtifactAccount);
			log.info("Given override file path -> " + tfVariableOverrideFileRepo);
			log.info("Given state repo -> " + spinStateRepo);
			log.info("Given unique user id -> " + uuId);
			log.info("Given current Component ->  Apply");

			if (StringUtils.isEmpty(spinArtifactAccount)) {
				log.error("Please specify artifact account it should'nt be blank or null.");
			}

			String currentUserDir = System.getProperty("user.home");
			String tfstatefilerepobasedir = currentUserDir + fileSeparator + "tfstatefilerepodir";
			String overrideVariableFilePath = null;

			String opsmxdir = currentUserDir + fileSeparator + ".opsmx";
			File opsmxDirFile = new File(opsmxdir);
			if (!opsmxDirFile.exists())
				opsmxDirFile.mkdir();

			File scriptDirFile = new File(opsmxDirFile.getPath() + fileSeparator + "script");
			if (!scriptDirFile.exists())
				scriptDirFile.mkdir();

			File terraformDestroySource = new File(scriptDirFile.getPath() + fileSeparator + "exeTerraformDestroy.sh");
			terraapputil.overWriteStreamOnFile(terraformDestroySource, getClass().getClassLoader()
					.getResourceAsStream(fileSeparator + "script" + fileSeparator + "exeTerraformDestroy.sh"));

			String configString = terraapputil.getConfig();
			JSONObject configObject = null;
			try {
				configObject = (JSONObject) parser.parse(configString);
			} catch (ParseException pe) {
				log.info("Exception while parsing config object :: " + configString);
				throw new RuntimeException("config Parse error:", pe);
			}
			JSONArray artifactAccounts = (JSONArray) configObject.get("artifactaccounts");
			JSONObject artifactAccount = null;

			for (int i = 0; i < artifactAccounts.size(); i++) {
				artifactAccount = (JSONObject) artifactAccounts.get(i);
				String githubArtifactaccountName = (String) artifactAccount.get("accountname");
				if (StringUtils.equalsIgnoreCase(githubArtifactaccountName.trim(), spinArtifactAccount.trim()))
					break;
			}

			String artifactType = (String) artifactAccount.get("artifacttype");

			String fullPathOfCurrentArtifactProviderImplClass = "com.opsmx.terraspin.component." + artifactType.trim()
					+ "Provider";

			ArtifactProvider currentArtifactProviderObj = null;

			try {
				currentArtifactProviderObj = (ArtifactProvider) Class
						.forName(fullPathOfCurrentArtifactProviderImplClass).newInstance();

			} catch (InstantiationException e2) {
				e2.printStackTrace();
			} catch (IllegalAccessException e2) {
				e2.printStackTrace();
			} catch (ClassNotFoundException e2) {
				e2.printStackTrace();
			}

			currentArtifactProviderObj.envSetup(artifactAccount);
			String spinStateRepoName = currentArtifactProviderObj.getArtifactSourceReopName(spinStateRepo);
			// String staterepoDirPath = currentUserDir + "/" + spinStateRepoName;
			String staterepoDirPath = tfstatefilerepobasedir + fileSeparator + spinStateRepoName;

			boolean isStateRepoGitcloned = currentArtifactProviderObj.pullStateArtifactSource(tfstatefilerepobasedir,
					spinStateRepoName, spinStateRepo);
			if (isStateRepoGitcloned) {

				String zipfilesrc = staterepoDirPath + "/" + uuId.trim() + ".zip";

				String extrapipelineidsrc = "/home/terraspin/extra/pipelineId-spinPipeId";
				File extrapipelineidsrcdir = new File(extrapipelineidsrc);
				if (!extrapipelineidsrcdir.exists())
					extrapipelineidsrcdir.mkdir();

				try {
					ziputil.unzip(zipfilesrc, extrapipelineidsrc);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				boolean ischangemod = processutil.runcommand("chmod 777 -R ~/extra");
				log.info("changing mod of file status :: " + ischangemod);

				if (StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
					terraservice.destroyStart(extrapipelineidsrc, "", null);
				} else {
					terraservice.destroyStart(extrapipelineidsrc, "", overrideVariableFilePath);
				}

				// JSONObject destroystatusobj = terraservice.destroyStatus("");
				JSONObject destroystatusobj = getDestroyStatus(currentTerraformInfraCodeDir);
				log.info("current destroystatusobj status :: " + destroystatusobj);
				String destroystatusstr = (String) destroystatusobj.get("status");

				if (StringUtils.equalsIgnoreCase("SUCCESS", destroystatusstr)) {

					currentArtifactProviderObj.pushStateArtifactSource(currentUserDir, spinStateRepoName,
							staterepoDirPath, uuId);

					writeDestroyStatus(currentTerraformInfraCodeDir, "SUCCESS");

				} else {
					log.info("----- error while executing spinterra destroy ------");
					writeDestroyStatus(currentTerraformInfraCodeDir, "TERMINAL");
				}

			} else {
				log.info("error during pulling state artifact source");
			}
		}
	}

}