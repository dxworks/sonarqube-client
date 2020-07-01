package org.dxworks.sonarqube.client.http.issue;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SonarIssueServiceIT {

	@Test
	void getAllIssuesAndComponentsForProjects() {
        SonarIssueService sonarIssueService = new SonarIssueService("http://localhost:9000");
        SonarIssuesResult sonarIssuesResult = sonarIssueService.getAllIssuesAndComponentsForProjects(singletonList("IG"), Arrays.asList());
        assertNotNull(sonarIssuesResult);
    }
}