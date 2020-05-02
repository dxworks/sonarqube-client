package org.dxworks.sonarqube.client.http.issue.dto;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum SonarIssueResolution {
    FALSE_POSITIVE("FALSE-POSITIVE"),
    WONTFIX("WONTFIX"),
    FIXED("FIXED"),
    REMOVED("REMOVED");

    private String name;

    SonarIssueResolution(String name) {
        this.name = name;
    }

    public static String names() {
        return Arrays.asList(values()).stream()
                .map(SonarIssueResolution::getName)
                .collect(Collectors.toList())
                .toString();
    }

    public static Optional<SonarIssueResolution> fromString(String name) {
        if (name != null) {
            for (SonarIssueResolution b : SonarIssueResolution.values()) {
                if (name.equalsIgnoreCase(b.name)) {
                    return Optional.of(b);
                }
            }
        }
        return Optional.empty();
    }

    public String getName() {
        return name;
    }
}
