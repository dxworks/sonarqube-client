package org.dxworks.sonarqube.client.http.issue.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class IssuesAndComponents {
	private Set<SonarIssue> issues;
	private Map<String, SonarComponent> components;
}
