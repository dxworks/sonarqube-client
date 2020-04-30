package org.dxworks.sonarqube.client.main.output;

import org.dxworks.sonarqube.client.http.issue.Component;
import org.dxworks.sonarqube.client.http.issue.Issue;
import org.dxworks.sonarqube.client.main.input.Axis;
import org.dxworks.sonarqube.client.main.input.Period;
import org.dxworks.sonarqube.client.main.input.Profile;
import org.dxworks.sonarqube.client.main.input.ProjectInput;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResultsGenerator {
    private final List<Issue> issues;
    private List<ProjectInput> projectInputs;
    private Period period;

    public ResultsGenerator(List<Issue> issues, List<ProjectInput> projectInputs, Period period) {
        this.issues = issues;
        this.projectInputs = projectInputs;
        this.period = period;
    }

    public Results getResults(Profile profile) {

        List<Result> open = getResultList(profile, issues, issue -> period.contains(issue.getCreationDate()));
        List<Result> closed = getResultList(profile, issues,
                issue -> issue.getCloseDate() != null && period.contains(issue.getCloseDate()));
        return Results.builder().open(open).closed(closed).build();
    }

    private List<Result> getResultList(Profile profile, List<Issue> issuesList, Predicate<Issue> filter) {
        Map<Axis, List<Issue>> axesToIssues = issuesList.stream()
                .filter(issue -> getAxisForRule(profile, issue.getRule()) != null)
                .collect(Collectors.groupingBy(issue -> getAxisForRule(profile, issue.getRule())));

        return axesToIssues.entrySet().stream()
                .flatMap(entry -> {
                    Map<Component, List<Issue>> filesToList = entry.getValue().stream().collect(Collectors.groupingBy(Issue::getComponent));
                    return filesToList.entrySet().stream()
                            .map(fileEntry -> Result.builder()
                                    .category(profile.getCategory())
                                    .file(getFilePath(fileEntry.getValue().stream().findFirst().get()))
                                    .name(entry.getKey().getName())
                                    .value(fileEntry.getValue().stream().mapToLong(Issue::getEffort).sum())
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
