package org.dxworks.sonarqube.client.http.project;

import org.dxworks.sonarqube.client.http.project.dto.SonarqubeProjectDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonarqubeProjectServiceTest {

	@org.junit.jupiter.api.Test
	void create() {
		SonarqubeProjectDTO testProject = SonarqubeProjectDTO.builder().name("Test project").key("TP-1")
				.visibility("public").build();
		SonarqubeProjectDTO createdProject = new SonarqubeProjectService("http://localhost:9000")
				.create(testProject.getKey(), testProject.getName(), testProject.getVisibility());
		testProject.forEach((key, value) -> assertEquals(value, createdProject.get(key)));
	}
}