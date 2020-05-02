package org.dxworks.sonarqube.client.main.output;

import com.google.api.client.util.Key;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Summary {
	@Key("Open Effort")
	private Long openedEffort;
	@Key("Closed Effort")
	private Long closedEffort;
	@Key("Current Effort")
	private Long currentTotalEffort;
}
