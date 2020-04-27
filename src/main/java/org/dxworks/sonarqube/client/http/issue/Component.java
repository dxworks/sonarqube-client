package org.dxworks.sonarqube.client.http.issue;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Component {
	private String key;
	private String path;
}
