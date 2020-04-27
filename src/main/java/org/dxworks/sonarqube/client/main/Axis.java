package org.dxworks.sonarqube.client.main;

import com.google.api.client.util.Key;
import lombok.Data;

import java.util.List;

@Data
public class Axis {
	@Key
	private String name;
	@Key
	private List<String> rules;
}
