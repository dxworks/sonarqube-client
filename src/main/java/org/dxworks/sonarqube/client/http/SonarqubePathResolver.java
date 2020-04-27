package org.dxworks.sonarqube.client.http;

import org.loose.fis.project.proofing.tool.http.PathResolver;

public class SonarqubePathResolver extends PathResolver {
	private static final String API = "api";

	public SonarqubePathResolver(String baseUrl) {
		super(baseUrl + "/" + API);
	}
}
