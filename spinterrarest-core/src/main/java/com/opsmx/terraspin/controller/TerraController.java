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

package com.opsmx.terraspin.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opsmx.terraspin.component.ApplyComponent;
import com.opsmx.terraspin.component.DestroyComponent;
import com.opsmx.terraspin.component.PlanComponent;
import com.opsmx.terraspin.service.TerraService;

@RestController
@RequestMapping("/api/v1")
public class TerraController {
	
	
	@Autowired
	PlanComponent pcs;
	
	@Autowired
	ApplyComponent acs;
	
	@Autowired
	DestroyComponent dcs;
	
	@Autowired
	TerraService ts;
	
	private static final Logger log = LoggerFactory.getLogger(TerraController.class);
	
	@RequestMapping(value = "/terraformPlan", method = RequestMethod.POST)
	public String startTerraform(HttpServletRequest request, @RequestBody String payload)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform plan payload :"+payload);
		return pcs.onTerraspinPlan(payload, baseURL);
	}

	//@RequestMapping(value = "/terraform/planStatus/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	//public String terraformPlanStatus(HttpServletRequest request, @PathVariable String applicationName,
	//		@PathVariable String pipelineName, @PathVariable String pipelineId)
	//		throws IOException, InterruptedException {
	//	String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
	//			+ request.getServerPort();
	//	log.info("terraform plan status :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
	//	//return ts.planStatus(applicationName, pipelineName, pipelineId, baseURL);
	//	return ts.planStatus(baseURL).toJSONString();
	//}

	//@RequestMapping(value = "/terraform/planOutput/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	//public String terraformPlanOutput(HttpServletRequest request, @PathVariable String applicationName,
	//		@PathVariable String pipelineName, @PathVariable String pipelineId)
	//		throws IOException, InterruptedException {
	//	String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
	//			+ request.getServerPort();
	//	log.info("terraform plan output :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
	//	//return ts.planOutput(applicationName, pipelineName, pipelineId, baseURL);
	//	return ts.planOutput(baseURL);
	//}

	@RequestMapping(value = "/terraform/planStatus", method = RequestMethod.GET)
	public String terraformPlanStatus(HttpServletRequest request)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform plan status");
		return ts.planStatus(baseURL).toJSONString();

	}
	
	@RequestMapping(value = "/terraform/planOutput", method = RequestMethod.GET)
	public String terraformPlanOutput(HttpServletRequest request)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform plan output");
		return ts.planOutput(baseURL);
	}
	
	@RequestMapping(value = "/terraformApply", method = RequestMethod.POST)
	public String applyTerraform(HttpServletRequest request, @RequestBody String payload)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform Apply payload :"+payload);
		return acs.onTerraspinApply(payload, baseURL);
	}

	//@RequestMapping(value = "/terraform/applyStatus/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	//public String terraformApplyStatus(HttpServletRequest request, @PathVariable String applicationName,
	//		@PathVariable String pipelineName, @PathVariable String pipelineId)
	//		throws IOException, InterruptedException {
	//	String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
	//			+ request.getServerPort();
	//	log.info("terraform apply status applicationName:"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
	//	//return ts.applyStatus(applicationName, pipelineName, pipelineId, baseURL);
	//	return ts.applyStatus(baseURL).toJSONString();
	//}
	
	//@RequestMapping(value = "/terraform/applyOutput/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	//public String terraformApplyOutput(HttpServletRequest request, @PathVariable String applicationName,
	//		@PathVariable String pipelineName, @PathVariable String pipelineId)
	//		throws IOException, InterruptedException {
	//	String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
	//			+ request.getServerPort();
	//	log.info("terraform apply output applicationName :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
	//	
	//	//return ts.applyOutput(applicationName, pipelineName, pipelineId, baseURL);
	//	return ts.applyOutput(baseURL);
	//}
	
	@RequestMapping(value = "/terraform/applyStatus", method = RequestMethod.GET)
	public String terraformApplyStatus(HttpServletRequest request)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform apply status");
		return ts.applyStatus(baseURL).toJSONString();

	}

	@RequestMapping(value = "/terraform/applyOutput", method = RequestMethod.GET)
	public String terraformApplyOutput(HttpServletRequest request)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform apply output");
		return ts.applyOutput(baseURL);
	}

	@RequestMapping(value = "/terraformDestroy", method = RequestMethod.POST)
	public String deleteTerraform(HttpServletRequest request, @RequestBody String payload)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform destroy payload :"+payload);
		return dcs.onTerraspinDestroy(payload, baseURL);
	}

	//@RequestMapping(value = "/terraform/destroyStatus/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	//public String terraformDeleteStatus(HttpServletRequest request, @PathVariable String applicationName,
	//		@PathVariable String pipelineName, @PathVariable String pipelineId)
	//		throws IOException, InterruptedException {
	//	String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
	//			+ request.getServerPort();
	//	log.info("terraform destroy status applicationName :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
	//	return ts.destroyStatus( baseURL).toJSONString();
	//}

	//@RequestMapping(value = "/terraform/destroyOutput/{applicationName}/{pipelineName}/{pipelineId}", method = RequestMethod.GET)
	//public String terraformDeleteOutput(HttpServletRequest request, @PathVariable String applicationName,
	//		@PathVariable String pipelineName, @PathVariable String pipelineId)
	//		throws IOException, InterruptedException {
	//	String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
	//			+ request.getServerPort();
	//	log.info("terraform destroy output applicationName :"+applicationName+"  pipelinename :"+pipelineName+" pipelienId :"+pipelineId);
	//	//return ts.destroyOutput(applicationName, pipelineName, pipelineId, baseURL);
	//	return ts.destroyOutput(baseURL);
	//}
	
	@RequestMapping(value = "/terraform/destroyStatus", method = RequestMethod.GET)
	public String terraformDeleteStatus(HttpServletRequest request)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform destroy status");
		return ts.destroyStatus( baseURL).toJSONString();

	}
	
	@RequestMapping(value = "/terraform/destroyOutput", method = RequestMethod.GET)
	public String terraformDeleteOutput(HttpServletRequest request)
			throws IOException, InterruptedException {
		String baseURL = request.getScheme().toString() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		log.info("terraform destroy output");
		return ts.destroyOutput(baseURL);
	}

}
