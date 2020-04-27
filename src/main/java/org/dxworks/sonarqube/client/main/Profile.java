package org.dxworks.sonarqube.client.main;

import com.google.api.client.util.Key;
import lombok.Data;

import java.util.List;

@Data
public class Profile {
	@Key
	private String category;
	@Key
	private List<Axis> axes;
}
