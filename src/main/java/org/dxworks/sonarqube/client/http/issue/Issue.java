package org.dxworks.sonarqube.client.http.issue;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class Issue {
	private String rule;
	private String project;
	private Long effort;

	private ZonedDateTime creationDate;
	private ZonedDateTime closeDate;

	private Component component;
}
