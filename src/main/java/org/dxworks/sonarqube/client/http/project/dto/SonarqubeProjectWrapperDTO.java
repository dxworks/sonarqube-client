package org.dxworks.sonarqube.client.http.project.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SonarqubeProjectWrapperDTO extends GenericJson {
	@Key
	private SonarqubeProjectDTO project;
}
