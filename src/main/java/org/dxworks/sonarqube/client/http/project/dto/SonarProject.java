package org.dxworks.sonarqube.client.http.project.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SonarProject extends GenericJson {
	@Key
	private String name;
	@Key
	private String key;
	@Key
	private String qualifier;
	@Key
	private String visibility;
}
