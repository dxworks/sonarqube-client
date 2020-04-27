package org.dxworks.sonarqube.client.http.issue.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SonarIssue extends GenericJson {
	@Key
	private String rule;
	@Key
	private String component;
	@Key
	private String project;
	@Key
	private String effort;
	@Key
	private String creationDate;
	@Key
	private String closeDate;
}
