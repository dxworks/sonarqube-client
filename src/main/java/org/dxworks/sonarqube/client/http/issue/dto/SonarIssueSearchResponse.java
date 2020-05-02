package org.dxworks.sonarqube.client.http.issue.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SonarIssueSearchResponse extends GenericJson {
    @Key
    private Long p;
    @Key
    private Long ps;
    @Key
    private Long total;
    @Key
    private Long effortTotal;

    @Key
    private List<SonarIssue> issues;
    @Key
    private List<SonarComponent> components;
}
