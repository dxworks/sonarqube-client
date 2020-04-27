package org.dxworks.sonarqube.client.http.project;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import lombok.SneakyThrows;
import org.dxworks.sonarqube.client.http.HttpService;
import org.dxworks.sonarqube.client.http.SonarqubePathResolver;
import org.dxworks.sonarqube.client.http.project.dto.SonarqubeProjectDTO;
import org.dxworks.sonarqube.client.http.project.dto.SonarqubeProjectWrapperDTO;

public class SonarqubeProjectService extends HttpService {
	public SonarqubeProjectService(String baseUrl) {
		super(new SonarqubePathResolver(baseUrl));
	}

	public SonarqubeProjectService(String baseUrl, HttpRequestInitializer httpRequestInitializer) {
		super(new SonarqubePathResolver(baseUrl), httpRequestInitializer);
	}

	public SonarqubeProjectDTO create(String key, String name) {
		return create(key, name, null);
	}

	@SneakyThrows
	public SonarqubeProjectDTO create(String key, String name, String visibility) {
		GenericUrl genericUrl = new GenericUrl(pathResolver.getApiPath("projects", "create"));
		genericUrl.put("name", name);
		genericUrl.put("project", key);
		if (visibility != null) {
			genericUrl.put("visibility", visibility);
		}
		HttpResponse response = httpClient.post(genericUrl);
		return response.parseAs(SonarqubeProjectWrapperDTO.class).getProject();
	}
}
