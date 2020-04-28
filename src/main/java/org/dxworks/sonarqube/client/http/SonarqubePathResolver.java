package org.dxworks.sonarqube.client.http;


import org.dxworks.utils.java.rest.client.PathResolver;

public class SonarqubePathResolver extends PathResolver {
	private static final String API = "api";

	public SonarqubePathResolver(String baseUrl) {
		super(baseUrl + "/" + API);
	}
}
