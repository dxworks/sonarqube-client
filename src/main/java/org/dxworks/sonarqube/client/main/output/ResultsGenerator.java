package org.dxworks.sonarqube.client.main.output;

import org.dxworks.sonarqube.client.http.issue.Component;
import org.dxworks.sonarqube.client.http.issue.Issue;
import org.dxworks.sonarqube.client.main.input.Axis;
import org.dxworks.sonarqube.client.main.input.Period;
import org.dxworks.sonarqube.client.main.input.Profile;
import org.dxworks.sonarqube.client.main.input.ProjectInput;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResultsGenerator {
    private final List<Issue> issues;
    private final List<ProjectInput> projectInputs;
    private final Period period;

    public ResultsGenerator(List<Issue> issues, List<ProjectInput> projectInputs, Period period) {
        this.issues = issues;
        this.projectInputs = projectInputs;
        this.period = period;
    }

    public Results getResults(Profile profile) {

        List<Result> open = getResultList(profile, issues, issue -> issueInPeriod(issue.getCreationDate()));
        List<Result> closed = getResultList(profile, issues,
                issue -> issue.getCloseDate() != null && issueInPeriod(issue.getCloseDate()));
        return Results.builder().open(open).closed(closed).build();
    }

    private boolean issueInPeriod(ZonedDateTime creationDate) {
        return period == null || period.contains(creationDate);
    }

    private List<Result> getResultList(Profile profile, List<Issue> issuesList, Predicate<Issue> issuePredicate) {
        Map<Axis, List<Issue>> axesToIssues = issuesList.stream()
                .filter(issuePredicate)
                .filter(issue -> getAxisForRule(profile, issue.getRule()) != null)
                .collect(Collectors.groupingBy(issue -> getAxisForRule(profile, issue.getRule())));

        return axesToIssues.entrySet().stream()
                .flatMap(entry -> {
                    Map<Component, List<Issue>> filesToList = entry.getValue().stream().collect(Collectors.groupingBy(Issue::getComponent));
                    return filesToList.values().stream()
                            .map(issueList -> Result.builder()
                                    .category(profile.getCategory())
                                    .file(getFilePath(issueList.stream().findFirst().get()))
                                    .name(entry.getKey().getName())
                                    .value(issueList.stream().mapToLong(Issue::getEffort).sum())
                                    .build());
                }).collect(Collectors.toList());
    }

    private Axis getAxisForRule(Profile profile, String rule) {
        return profile.getAxes().stream()
                .filter(axis -> axis.getRules().contains(rule))
                .findAny()
                .orElse(null);
    }

    private String getFilePath(Issue issue) {
        return Stream.concat(Stream.of(getPrefixOrNull(issue)).filter(Objects::nonNull),
                Stream.of(issue.getComponent().getPath())).collect(Collectors.joining("/"));
    }

    private String getPrefixOrNull(Issue issue) {
        return projectInputs.stream()
                .filter(projectInput -> projectInput.getKey().equals(issue.getProject()))
                .findFirst()
                .map(ProjectInput::getPrefix)
                .orElse(null);
    }

}
