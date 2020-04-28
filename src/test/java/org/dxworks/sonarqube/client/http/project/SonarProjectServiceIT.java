package org.dxworks.sonarqube.client.http.project;

import org.dxworks.sonarqube.client.http.project.dto.SonarProject;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonarProjectServiceIT {

	@org.junit.jupiter.api.Test
	void create() {
		SonarProject testProject = SonarProject.builder().name("Test project").key("TP-1").visibility("public").build();
		SonarProject createdProject = new SonarProjectService("http://localhost:9000")
				.create(testProject.getKey(), testProject.getName(), testProject.getVisibility());
		testProject.forEach((key, value) -> assertEquals(value, createdProject.get(key)));
	}
}
