package org.dxworks.sonarqube.client.http.issue;

import lombok.Data;

import java.util.List;

@Data
public class SonarIssuesResult {
    private Long total;
    private Long totalEffort;
    private List<Issue> issues;

    public SonarIssuesResult(Long totalEffort, List<Issue> issues) {
        this.totalEffort = totalEffort;
        this.issues = issues;
        this.total = (long) this.issues.size();
    }
}
