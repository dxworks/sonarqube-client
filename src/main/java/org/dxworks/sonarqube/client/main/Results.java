package org.dxworks.sonarqube.client.main;

import com.google.api.client.util.Key;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Results {
	@Key
	private List<Result> open;
	@Key
	private List<Result> closed;
}
