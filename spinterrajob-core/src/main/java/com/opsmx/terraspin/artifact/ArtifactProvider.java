package com.opsmx.terraspin.artifact;

import org.json.simple.JSONObject;

public abstract class ArtifactProvider {
	
	public abstract void envSetup(JSONObject artifactAccount);

	public abstract String getArtifactSourceReopName (String artifactSourceReopPath);
	
	public abstract String getArtifactSourceReopNameWithUsername (String artifactSourceReopPath);
	
	public abstract String getOverrideFileNameWithPath (String tfVariableOverrideFileRepo);
	
	public abstract boolean cloneOverrideFile (String cloneDir, String tfVariableOverrideFileRepoName);
	
	public abstract void pushStateArtifactSource(String currentUserDir, String spinStateRepoName, String staterepoDirPath, String uuId);
	
	public abstract boolean pullStateArtifactSource(String cloneDir, String spinStateRepoName, String spinStateRepoNameWithUserName, String uuId, String componentType);

}
