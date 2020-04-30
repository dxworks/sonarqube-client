package org.dxworks.sonarqube.client.main;

import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.dxworks.sonarqube.client.http.ce.SonarTaskService;
import org.dxworks.sonarqube.client.http.ce.TaskStatus;
import org.dxworks.sonarqube.client.http.issue.Issue;
import org.dxworks.sonarqube.client.http.issue.SonarIssueService;
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

        boolean tasksFinishedSuccessfully = checkTasksHaveCompletedSuccessfully(sonarClientConfig.getBaseUrl(), sonarClientConfig.getTasks());
        if (!tasksFinishedSuccessfully) {
            System.err.println("Tasks have failed! Aborting analysis...");
            System.exit(1);
        }

        List<Issue> issues = new SonarIssueService(sonarClientConfig.getBaseUrl()).getAllIssuesAndComponentsForProjects(
                sonarClientConfig.getProjectInputs().stream()
                        .map(ProjectInput::getKey)
                        .collect(Collectors.toList()));

        Results results = new ResultsGenerator(issues, sonarClientConfig.getProjectInputs(), sonarClientConfig.getPeriod()).getResults(sonarClientConfig.getProfile());
        sonarClientConfig.getPathToOutput().toFile().mkdirs();
        writeOutput(sonarClientConfig.getPathToOutput(), sonarClientConfig.getOutputFilesPrefix(), results);
    }

    public static <T> CompletableFuture<Boolean> noneMatch(List<? extends CompletionStage<T>> l, Predicate<T> criteria) {
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
            result.complete(true);
            System.out.println("Getting task resolution has failed!");
            t.printStackTrace();
        }
        result.complete(false);
    }

    @SneakyThrows
    private static boolean checkTasksHaveCompletedSuccessfully(String baseUrl, List<String> tasks) {
        return noneMatch(CollectionUtils.emptyIfNull(tasks).parallelStream()
                .map(taskID -> CompletableFuture.supplyAsync(() -> getResolution(baseUrl, taskID))).collect(Collectors.toList()), TaskStatus::hasFailed).get();
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
                Summary.builder().openEffort(getSumOfValues(results.getOpen()))
                        .closedEffort(getSumOfValues(results.getClosed())).build());
    }

    private static long getSumOfValues(List<Result> resultList) {
        return resultList.stream().mapToLong(Result::getValue).sum();
    }

    private static String getPrefixedFilename(String prefix, String name) {
        return Stream.concat(Stream.of(prefix).filter(Objects::nonNull), Stream.of(name))
                .collect(Collectors.joining("-"));
    }

    private static TaskStatus getResolution(String baseUrl, String taskID) {
        try {
            SonarTaskService sonarTaskService = new SonarTaskService(baseUrl);
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
