package org.dxworks.sonarqube.client.http.issue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.Data;
import lombok.SneakyThrows;
import org.dxworks.sonarqube.client.http.HttpService;
import org.dxworks.sonarqube.client.http.SonarqubePathResolver;
import org.dxworks.sonarqube.client.http.issue.dto.SonarComponent;
import org.dxworks.sonarqube.client.http.issue.dto.SonarIssue;
import org.dxworks.sonarqube.client.http.issue.dto.SonarIssueSearchResponse;
import org.loose.fis.project.proofing.tool.http.PathResolver;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SonarIssueService extends HttpService {

	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

	public SonarIssueService(String baseUrl) {
		super(new SonarqubePathResolver(baseUrl));
	}

	public SonarIssueService(String baseUrl, HttpRequestInitializer httpRequestInitializer) {
		super(new PathResolver(baseUrl), httpRequestInitializer);
	}

	@SneakyThrows
	public List<Issue> getAllIssuesAndComponentsForProjects(List<String> projectKeys) {
		GenericUrl genericUrl = new GenericUrl(pathResolver.getApiPath("issues", "search"));
		genericUrl.put("componentKeys", String.join(",", projectKeys));
		Set<SonarIssue> allIssues = new HashSet<>();
		Map<String, SonarComponent> allComponents = new HashMap<>();

		SonarIssueSearchResponse searchResponse;
		Long currentPage = 1L;
		Long pageSize = 500L;
		do {
			genericUrl.set("p", currentPage);
			genericUrl.set("ps", pageSize);
			HttpResponse response = httpClient.get(genericUrl);
			searchResponse = response.parseAs(SonarIssueSearchResponse.class);
			allIssues.addAll(searchResponse.getIssues());
			allComponents.putAll(searchResponse.getComponents().stream()
					.collect(Collectors.toMap(SonarComponent::getKey, Function.identity())));
			currentPage++;
		} while (searchResponse.getP() * searchResponse.getPs() < searchResponse.getTotal());

		return allIssues.stream()
				.map(sonarIssue -> Issue.builder().rule(sonarIssue.getRule()).project(sonarIssue.getProject())
						.component(getComponent(allComponents, sonarIssue)).effort(getEffort(sonarIssue.getEffort()))
						.creationDate(getDate(sonarIssue.getCreationDate()))
						.closeDate(getDate(sonarIssue.getCloseDate())).build()).collect(Collectors.toList());
	}

	private Long getEffort(String effort) {
		long min;
		try {
			min = Long.parseLong(effort.split("min")[0]);
		} catch (Exception e) {
			min = 0L;
		}
		return min;
	}

	private ZonedDateTime getDate(String creationDate) {
		if (creationDate == null || Data.isNull(creationDate))
			return null;
		return ZonedDateTime.parse(creationDate, dateFormatter);
	}

	private Component getComponent(Map<String, SonarComponent> allComponents, SonarIssue sonarIssue) {
		SonarComponent sonarComponent = allComponents.get(sonarIssue.getComponent());
		return Component.builder().key(sonarComponent.getKey()).path(sonarComponent.getPath()).build();
	}
}
