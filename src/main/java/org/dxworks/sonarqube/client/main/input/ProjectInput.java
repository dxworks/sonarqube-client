package org.dxworks.sonarqube.client.main.input;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectInput {
	private String key;
	private String prefix;
}
