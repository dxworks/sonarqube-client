package org.dxworks.sonarqube.client.http.issue.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SonarComponent extends GenericJson {
	@Key
	private String key;
	@Key
	private String path;
}
