package org.dxworks.sonarqube.client.http.project;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import lombok.SneakyThrows;
import org.dxworks.sonarqube.client.http.HttpService;
import org.dxworks.sonarqube.client.http.SonarqubePathResolver;
import org.dxworks.sonarqube.client.http.project.dto.SonarProject;
import org.dxworks.sonarqube.client.http.project.dto.SonarProjectWrapper;

public class SonarProjectService extends HttpService {
	public SonarProjectService(String baseUrl) {
		super(new SonarqubePathResolver(baseUrl));
	}

	public SonarProjectService(String baseUrl, HttpRequestInitializer httpRequestInitializer) {
		super(new SonarqubePathResolver(baseUrl), httpRequestInitializer);
	}

	public SonarProject create(String key, String name) {
		return create(key, name, null);
	}

	@SneakyThrows
	public SonarProject create(String key, String name, String visibility) {
		GenericUrl genericUrl = new GenericUrl(pathResolver.getApiPath("projects", "create"));
		genericUrl.put("name", name);
		genericUrl.put("project", key);
		if (visibility != null) {
			genericUrl.put("visibility", visibility);
		}
		HttpResponse response = httpClient.post(genericUrl);
		return response.parseAs(SonarProjectWrapper.class).getProject();
	}
}
