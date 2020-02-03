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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.service.TerraService;
import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;
import com.opsmx.terraspin.util.ZipUtil;

@Component
public class PlanComponent {

	/**
	 * This method is called during Spring's startup.
	 * 
	 * @param event Event raised when an ApplicationContext gets initialized or
	 *              refreshed.
	 */
	private static final Logger log = LoggerFactory.getLogger(PlanComponent.class);
	static final String separator = File.separator;
	JSONParser parser = new JSONParser();


	public void onApplicationEvent() {

		String currentUserDir = System.getProperty("user.home");
		String spinPlan = System.getenv("plan");
		String spinArtifactAccount = System.getenv("artifactAccount");
		// String spincloudAccount = System.getenv("cloudAccount");
		String spinStateRepo = System.getenv("stateRepo");
		String tfVariableOverrideFileRepo = System.getenv("variableOverrideFileRepo");
		String uuId = System.getenv("uuId");

		log.info("current user :: " + System.getProperty("user.name") + " and user dir :: "
				+ System.getProperty("user.home"));
		log.info("spinPlan:" + spinPlan);
		log.info("spinArtifactAccount:" + spinArtifactAccount);
		log.info("tfVariableOverrideFileRepo:" + tfVariableOverrideFileRepo);
		log.info("spinStateRepo:" + spinStateRepo);
		log.info("uuId:" + uuId);

		String spinStateRepoName = spinStateRepo.trim().split(".git")[0];
		String staterepoDirPath = currentUserDir + "/" + spinStateRepoName;
		String tfVariableOverrideFileRepoName = new String();
		String tfVariableOverrideFileName = new String();

		TerraAppUtil terraapputil = new TerraAppUtil();
		TerraService terraservice = new TerraService();
		ProcessUtil processutil = new ProcessUtil();
		ZipUtil ziputil = new ZipUtil();

		String opsmxdir = currentUserDir + separator + ".opsmx";
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File scriptDirFile = new File(opsmxDirFile.getPath() + separator + "script");
		if (!scriptDirFile.exists())
			scriptDirFile.mkdir();
		
		File terraformInitSource = new File(scriptDirFile.getPath() + separator + "exeTerraformInit.sh");
		terraapputil.overWriteStreamOnFile(terraformInitSource, getClass().getClassLoader()
				.getResourceAsStream(separator + "script" + separator + "exeTerraformInit.sh"));

		File terraformPlanSource = new File(scriptDirFile.getPath() + separator + "exeTerraformPlan.sh");
		terraapputil.overWriteStreamOnFile(terraformPlanSource, getClass().getClassLoader()
				.getResourceAsStream(separator + "script" + separator + "exeTerraformPlan.sh"));

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
			String artifactaccountName = (String) artifactAccount.get("accountname");
			if (StringUtils.equalsIgnoreCase(artifactaccountName.trim(), spinArtifactAccount.trim()))
				break;
		}

		if (!StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
			tfVariableOverrideFileRepoName = tfVariableOverrideFileRepo.trim().split(".git")[0];
			tfVariableOverrideFileName = tfVariableOverrideFileRepo.trim().split("//")[1];
		}
		
		if (!artifactAccount.isEmpty()) {

			String gitUser = (String) artifactAccount.get("username");
			String gittoken = (String) artifactAccount.get("token");
			String gitPass = (String) artifactAccount.get("password");

			String checkTfFileStateRepoPresentCommand = "curl -u GITUSER:GITPASS https://api.github.com/GITUSER/REPONAME";
			String tfFileStateRepoGitCloneCommand = "git clone https://GITUSER:GITPASS@github.com/GITUSER/REPONAME";

			String checkTfVariableOverrideFileRepoPresentCommand = "curl -u GITUSER:GITPASS https://api.github.com/GITUSER/REPONAME";
			String tfVariableOverrideFileGitCloneCommand = "git clone https://GITUSER:GITPASS@github.com/GITUSER/REPONAME";

			if (StringUtils.isNoneEmpty(gitPass)) {
				checkTfFileStateRepoPresentCommand = checkTfFileStateRepoPresentCommand.replaceAll("GITUSER", gitUser)
						.replaceAll("GITPASS", gitPass).replaceAll("REPONAME", spinStateRepo);
				tfFileStateRepoGitCloneCommand = tfFileStateRepoGitCloneCommand.replaceAll("GITUSER", gitUser)
						.replaceAll("GITPASS", gitPass).replaceAll("REPONAME", spinStateRepo);

				checkTfVariableOverrideFileRepoPresentCommand = checkTfVariableOverrideFileRepoPresentCommand
						.replaceAll("GITUSER", gitUser).replaceAll("GITPASS", gitPass)
						.replaceAll("REPONAME", tfVariableOverrideFileRepoName);
				tfVariableOverrideFileGitCloneCommand = tfVariableOverrideFileGitCloneCommand.replaceAll("GITUSER", gitUser)
						.replaceAll("GITPASS", gitPass).replaceAll("REPONAME", tfVariableOverrideFileRepoName);

			} else {
				checkTfFileStateRepoPresentCommand = checkTfFileStateRepoPresentCommand.replaceAll("GITUSER", gitUser)
						.replaceAll("GITPASS", gittoken).replaceAll("REPONAME", spinStateRepo);
				tfFileStateRepoGitCloneCommand = tfFileStateRepoGitCloneCommand.replaceAll("GITUSER", gitUser)
						.replaceAll("GITPASS", gittoken).replaceAll("REPONAME", spinStateRepo);

				checkTfVariableOverrideFileRepoPresentCommand = checkTfVariableOverrideFileRepoPresentCommand
						.replaceAll("GITUSER", gitUser).replaceAll("GITPASS", gittoken)
						.replaceAll("REPONAME", tfVariableOverrideFileRepoName);
				tfVariableOverrideFileGitCloneCommand = tfVariableOverrideFileGitCloneCommand.replaceAll("GITUSER", gitUser)
						.replaceAll("GITPASS", gittoken).replaceAll("REPONAME", tfVariableOverrideFileRepoName);
			}

			if (StringUtils.isEmpty(tfVariableOverrideFileRepo)) {
				// plan start
				terraservice.planStart(artifactAccount, null);

			} else {
				// plan start
				boolean isOverrideVariableFileRepoPresent = processutil
						.runcommand(checkTfVariableOverrideFileRepoPresentCommand);
				log.info("checking is variable overide file repo present :: " + isOverrideVariableFileRepoPresent);

				if (isOverrideVariableFileRepoPresent && !StringUtils.isEmpty(tfVariableOverrideFileRepo)) {

					String overrideVariableFiledestination = "/home/terraspin/extra";
										
					boolean isOverrideVariableRepoGitcloned = processutil.runcommandwithindir(tfVariableOverrideFileGitCloneCommand, overrideVariableFiledestination);
					log.info("is overide variable file git repo cloned :: " + isOverrideVariableRepoGitcloned);

					if (isOverrideVariableRepoGitcloned) {
						String overrideVariableFilePath = overrideVariableFiledestination + "/"
								+ tfVariableOverrideFileRepoName + "/" + tfVariableOverrideFileName;
						terraservice.planStart(artifactAccount, overrideVariableFilePath);

					} else {
						log.info("error : " + processutil.getStatusRootObj());
					}
				} else {
					log.info("error : " + processutil.getStatusRootObj());
				}
			}

			JSONObject planstatusobj = terraservice.planStatus("");
			log.info("----- current plan status :: " + planstatusobj);
			String planstatusstr = (String) planstatusobj.get("status");

			if (StringUtils.equalsIgnoreCase("SUCCESS", planstatusstr)) {

				boolean isStateRepoPresent = processutil.runcommand(checkTfFileStateRepoPresentCommand);
				log.info("checking is state repo present :: " + isStateRepoPresent);

				if (isStateRepoPresent) {
					boolean isStateRepoGitcloned = processutil.runcommandwithindir(tfFileStateRepoGitCloneCommand,
							currentUserDir);
					log.info("is state repo cloned :: " + isStateRepoGitcloned);

					if (isStateRepoGitcloned) {
						String source1 = "/home/terraspin/" + spinStateRepoName + "/.git";
						File srcDir1 = new File(source1);
						String destination1 = "/home/terraspin/extra";
						File destDir1 = new File(destination1);

						try {
							FileUtils.copyDirectoryToDirectory(srcDir1, destDir1);
						} catch (IOException e) {
							e.printStackTrace();
						}

						File staterepoDir = new File(staterepoDirPath);

						try {
							FileUtils.cleanDirectory(staterepoDir);
						} catch (IOException e) {
							e.printStackTrace();
						}

						String source2 = "/home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";
						File srcDir2 = new File(source2);
						String destination2 = staterepoDirPath;
						File destDir2 = new File(destination2);

						try {
							FileUtils.copyDirectoryToDirectory(srcDir2, destDir2);
						} catch (IOException e) {
							e.printStackTrace();
						}

						String source3 = "/home/terraspin/extra/.git";
						File srcDir3 = new File(source3);
						String destination3 = staterepoDirPath;
						File destDir3 = new File(destination3);

						try {
							FileUtils.copyDirectoryToDirectory(srcDir3, destDir3);
						} catch (IOException e) {
							e.printStackTrace();
						}

						/*
						 * String zippath = staterepoDirPath + "/pipelineId-spinPipeId.zip"; String
						 * srczippath = staterepoDirPath + "/pipelineId-spinPipeId";
						 */

						String zippath = staterepoDirPath + "/" + uuId.trim() + ".zip";
						String srczippath = staterepoDirPath + "/pipelineId-spinPipeId";

						try {
							ziputil.zipDirectory(srczippath, zippath);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						try {
							FileUtils.deleteDirectory(new File(srczippath));
						} catch (IOException e) {
							e.printStackTrace();
						}

						String gitconfigusernamecommand = "git config --global user.name \"OpsMx\"";
						boolean isgitconfigusernamecommandsuccess = processutil
								.runcommandwithindir(gitconfigusernamecommand, staterepoDirPath);
						log.info("isgitconfigusername command got success :: " + isgitconfigusernamecommandsuccess);
						// log.info("error :: " + processutil.getStatusRootObj());

						String gitconfiguseremailcommand = "git config --global user.email \"Team@OpsMx.com\"";
						boolean isconfiguseremailcommandsuccess = processutil
								.runcommandwithindir(gitconfiguseremailcommand, staterepoDirPath);
						log.info("isconfiguseremailcommand got success :: " + isconfiguseremailcommandsuccess);
						// log.info("error :: " + processutil.getStatusRootObj());

						String gitaddcommand = "git add .";
						boolean isgitaddcommandsuccess = processutil.runcommandwithindir(gitaddcommand,
								staterepoDirPath);
						log.info("isgitadd command got success :: " + isconfiguseremailcommandsuccess);

						if (isgitaddcommandsuccess) {
							String gitcommitcommand = "git commit -m \"adding spinterra plan state\"";
							boolean isgitcommitcommandsuccess = processutil.runcommandwithindir(gitcommitcommand,
									staterepoDirPath);
							log.info("isgitcommit command got success :: " + isgitcommitcommandsuccess);

							if (isgitcommitcommandsuccess) {
								String gitpushcommand = "git push -u origin master";
								boolean isgitpushcommandsuccess = processutil.runcommandwithindir(gitpushcommand,
										staterepoDirPath);

								if (isgitpushcommandsuccess) {
									log.info("gitpushcommand got success : ");
								} else {
									log.info("error : " + processutil.getStatusRootObj());
								}
							} else {
								log.info("error : " + processutil.getStatusRootObj());
							}
						} else {
							log.info("error : " + processutil.getStatusRootObj());
						}
					} else {
						log.info("error : " + processutil.getStatusRootObj());
					}
				} else {
					log.info("error : " + processutil.getStatusRootObj());
				}
			} else {
				log.info("error : " + processutil.getStatusRootObj());
			}

		} else {

			log.info("artifact config is not present:");
		}
	}
}