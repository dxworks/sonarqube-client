package org.dxworks.sonarqube.client.main;

import com.google.api.client.http.HttpRequestInitializer;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.dxworks.sonarqube.client.http.ce.SonarTaskService;
import org.dxworks.sonarqube.client.http.ce.TaskStatus;
import org.dxworks.sonarqube.client.http.issue.Issue;
import org.dxworks.sonarqube.client.http.issue.SonarIssueService;
import org.dxworks.sonarqube.client.http.issue.SonarIssuesResult;
import org.dxworks.sonarqube.client.http.rule.SonarRulesService;
import org.dxworks.sonarqube.client.main.input.ProjectInput;
import org.dxworks.sonarqube.client.main.output.Result;
import org.dxworks.sonarqube.client.main.output.Results;
import org.dxworks.sonarqube.client.main.output.ResultsGenerator;
import org.dxworks.sonarqube.client.main.output.Summary;
import org.dxworks.utils.java.rest.client.utils.JsonMapper;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final JsonMapper jsonMapper = new JsonMapper();

    public static void main(String... args) {
        SonarClientConfig sonarClientConfig = new SonarClientConfig(args);

        HttpRequestInitializer requestInitializer = sonarClientConfig.getRequestInitializer();

        boolean tasksFinishedSuccessfully = checkTasksHaveCompletedSuccessfully(sonarClientConfig.getBaseUrl(), sonarClientConfig.getTasks(), requestInitializer);
        if (!tasksFinishedSuccessfully) {
            System.err.println("Tasks have failed! Aborting analysis...");
            System.exit(1);
        }


        SonarRulesService sonarRulesService = new SonarRulesService(sonarClientConfig.getBaseUrl(), requestInitializer);


        List<SonarIssuesResult> issueResults = sonarClientConfig.getProfile().getAxes().stream()
                .map(axis -> {
                    List<String> existingRules = filterExistingRules(axis.getRules(), sonarRulesService);
                    if (existingRules.isEmpty()) {
                        System.out.printf("Skipping axis %s since there are no rules in this Sonarqube instance \n", axis.getName());
                        return null;
                    }
                    System.out.printf("Getting Issues for axis %s with rules: %s\n", axis.getName(), axis.getRules());
                    return existingRules;
                })
                .filter(Objects::nonNull)
                .map(rules -> new SonarIssueService(sonarClientConfig.getBaseUrl(), requestInitializer).getAllIssuesAndComponentsForProjects(
                        sonarClientConfig.getProjectInputs().stream()
                                .map(ProjectInput::getKey)
                                .collect(Collectors.toList()), rules))
                .collect(Collectors.toList());

        List<Issue> issues = issueResults.stream()
                .flatMap(sonarIssuesResult -> sonarIssuesResult.getIssues().stream())
                .collect(Collectors.toList());

        Results results = new ResultsGenerator(issues, sonarClientConfig.getProjectInputs(), sonarClientConfig.getPeriod()).getResults(sonarClientConfig.getProfile());
        results.setTotalEffort(issueResults.stream().mapToLong(SonarIssuesResult::getTotalEffort).sum());
        sonarClientConfig.getPathToOutput().toFile().mkdirs();
        writeOutput(sonarClientConfig.getPathToOutput(), sonarClientConfig.getOutputFilesPrefix(), results);
    }

    private static List<String> filterExistingRules(List<String> rules, SonarRulesService sonarRulesService) {
        return rules.stream()
                .filter(rule -> {
                    if (sonarRulesService.getRule(rule).isPresent())
                        return true;
                    System.err.printf("Rule %s could not be found on this Sonarqube instance.\n", rule);
                    return false;
                })
                .collect(Collectors.toList());
    }

    private static <T> CompletableFuture<Boolean> noneMatch(List<? extends CompletionStage<T>> l, Predicate<T> criteria) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Consumer<T> whenMatching = v -> {
            if (criteria.test(v))
                result.complete(false);
        };
        CompletableFuture.allOf(l.stream()
                .map(f -> f.thenAccept(whenMatching))
                .toArray(CompletableFuture<?>[]::new))
                .whenComplete((ignored, t) -> handleTaskResolutionResult(result, t));
        return result;
    }

    private static void handleTaskResolutionResult(CompletableFuture<Boolean> result, Throwable t) {
        if (t != null) {
            result.complete(false);
            System.out.println("Getting task resolution has failed!");
            t.printStackTrace();
        }
        result.complete(true);
    }

    @SneakyThrows
    private static boolean checkTasksHaveCompletedSuccessfully(String baseUrl, List<String> tasks, HttpRequestInitializer requestInitializer) {
        return noneMatch(CollectionUtils.emptyIfNull(tasks).parallelStream()
                .map(taskID -> CompletableFuture.supplyAsync(() -> getResolution(baseUrl, taskID, requestInitializer))).collect(Collectors.toList()), TaskStatus::hasFailed).get();
    }

    @SneakyThrows
    private static void writeOutput(Path pathToOutput, String outputFilesPrefix, Results results) {
        File openFile = pathToOutput.resolve(getPrefixedFilename(outputFilesPrefix, "open.json")).toFile();
        File closedFile = pathToOutput.resolve(getPrefixedFilename(outputFilesPrefix, "closed.json")).toFile();
        jsonMapper.writeJSON(new FileWriter(openFile), results.getOpen());
        jsonMapper.writeJSON(new FileWriter(closedFile), results.getClosed());
        printSummary(pathToOutput, outputFilesPrefix, results);
    }

    @SneakyThrows
    private static void printSummary(Path pathToOutput, String outputFilesPrefix, Results results) {
        File summaryFile = pathToOutput.resolve(getPrefixedFilename(outputFilesPrefix, "summary.json")).toFile();
        jsonMapper.writeJSON(new FileWriter(summaryFile),
                Summary.builder().openedEffort(getSumOfValues(results.getOpen()))
                        .closedEffort(getSumOfValues(results.getClosed()))
                        .currentTotalEffort(results.getTotalEffort()).build());
    }

    private static long getSumOfValues(List<Result> resultList) {
        return resultList.stream().mapToLong(Result::getValue).sum();
    }

    private static String getPrefixedFilename(String prefix, String name) {
        return Stream.concat(Stream.of(prefix).filter(Objects::nonNull), Stream.of(name))
                .collect(Collectors.joining("-"));
    }

    private static TaskStatus getResolution(String baseUrl, String taskID, HttpRequestInitializer requestInitializer) {
        try {
            SonarTaskService sonarTaskService = new SonarTaskService(baseUrl, requestInitializer);
            while (true) {
                TaskStatus taskStatus = sonarTaskService.getTaskStatus(taskID);
                System.out.printf("Task %s has status %s\n", taskID, taskStatus.name());
                if (taskStatus.isFinished()) {
                    return taskStatus;
                }
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
