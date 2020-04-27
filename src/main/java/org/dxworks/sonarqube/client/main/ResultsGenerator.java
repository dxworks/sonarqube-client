package org.dxworks.sonarqube.client.main;

import org.dxworks.sonarqube.client.http.issue.Component;
import org.dxworks.sonarqube.client.http.issue.Issue;

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

	ResultsGenerator(List<Issue> issues, List<ProjectInput> projectInputs, Period period) {
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
		Map<Component, List<Issue>> componentIssueMap = issuesList.stream().filter(filter)
				.collect(Collectors.groupingBy(Issue::getComponent));

		return componentIssueMap.entrySet().stream().flatMap(componentToIssue -> {
			Map<String, List<Issue>> axisToIssues = getAxisToIssuesMap(componentToIssue.getValue(), profile);
			return axisToIssues.entrySet().stream().map(axisToIssue -> Result.builder().category(profile.getCategory())
					.file(getFilePath(componentToIssue.getValue().get(0))).name(axisToIssue.getKey())
					.value(axisToIssue.getValue().stream().mapToLong(Issue::getEffort).sum()).build());
		}).collect(Collectors.toList());
	}

	private Map<String, List<Issue>> getAxisToIssuesMap(List<Issue> issues, Profile profile) {
		return profile.getAxes().stream()
				.collect(Collectors.toMap(Axis::getName, axis -> getIssuesForAxis(axis, issues)));
	}

	private List<Issue> getIssuesForAxis(Axis axis, List<Issue> issues) {
		return issues.stream().filter(issue -> axis.getRules().contains(issue.getRule())).collect(Collectors.toList());
	}

	private String getFilePath(Issue issue) {
		return Stream.concat(Stream.of(getPrefixOrNull(issue)).filter(Objects::nonNull),
				Stream.of(issue.getComponent().getPath())).collect(Collectors.joining("/"));
	}

	private String getPrefixOrNull(Issue issue) {
		return projectInputs.stream().filter(projectInput -> projectInput.getKey().equals(issue.getProject()))
				.findFirst().map(ProjectInput::getPrefix).orElse(null);
	}

}
