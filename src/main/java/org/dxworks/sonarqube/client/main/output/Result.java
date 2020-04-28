package org.dxworks.sonarqube.client.main.output;

import com.google.api.client.util.Key;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
	@Key
	private String file;
	@Key
	private String name;
	@Key
	private String category;
	@Key
	private Long value;
}
