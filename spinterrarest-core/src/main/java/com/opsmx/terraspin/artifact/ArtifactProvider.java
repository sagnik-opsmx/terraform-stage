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

package com.opsmx.terraspin.artifact;

import org.json.simple.JSONObject;

public abstract class ArtifactProvider {
	public abstract void envSetup(JSONObject artifactAccount);

	public abstract String getArtifactSourceReopName (String artifactSourceReopPath);
	
	public abstract String getArtifactSourceReopNameWithUsername (String artifactSourceReopPath);
	
	public abstract String getOverrideFileNameWithPath (String tfVariableOverrideFileRepo);
	
	public abstract boolean cloneOverrideFile (String cloneDir, String tfVariableOverrideFileRepoName, JSONObject artifactAccount);
	
	public abstract void pushStateArtifactSource(String currentUserDir, String spinStateRepoName, String staterepoDirPath, String uuId);
	
	public abstract boolean pullStateArtifactSource(String cloneDir, String spinStateRepoName, String spinStateRepoNameWithUserName, String uuId, String componentType, JSONObject artifactAccount);

}
