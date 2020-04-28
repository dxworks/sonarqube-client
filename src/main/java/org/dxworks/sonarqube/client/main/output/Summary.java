package org.dxworks.sonarqube.client.main.output;

import com.google.api.client.util.Key;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Summary {
	@Key
	private Long openEffort;
	@Key
	private Long closedEffort;
}
