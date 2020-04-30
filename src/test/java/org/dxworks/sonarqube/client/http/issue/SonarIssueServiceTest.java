package org.dxworks.sonarqube.client.http.issue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonarIssueServiceTest {

    private SonarIssueService sonarIssueService = new SonarIssueService(null);

    @Test
    void testGetEffortInMinutes() {
        assertEquals(1 * 60 + 30, sonarIssueService.getEffort("1h30min"));
        assertEquals(4 * 60 + 50, sonarIssueService.getEffort("4h50min"));
        assertEquals(24 * 60 + 37, sonarIssueService.getEffort("1d37min"));
        assertEquals(1 * 7 * 24 * 60 + 2 * 24 * 60 + 5 * 60 + 30, sonarIssueService.getEffort("1w 2d 5h 30min"));
    }
}