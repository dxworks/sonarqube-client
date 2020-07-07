package org.dxworks.sonarqube.client.http.issue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.util.Data;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.dxworks.sonarqube.client.http.SonarService;
import org.dxworks.sonarqube.client.http.issue.dto.SonarComponent;
import org.dxworks.sonarqube.client.http.issue.dto.SonarIssue;
import org.dxworks.sonarqube.client.http.issue.dto.SonarIssueSearchResponse;
import org.dxworks.utils.java.rest.client.response.HttpResponse;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SonarIssueService extends SonarService {

    public static final ImmutableMap<String, Integer> UNITS_TO_VALUES = ImmutableMap.of(
            "w", 7 * 24 * 60,
            "d", 24 * 60,
            "h", 60,
            "min", 1);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    public SonarIssueService(String baseUrl) {
        super(baseUrl);
    }

    public SonarIssueService(String baseUrl, HttpRequestInitializer httpRequestInitializer) {
        super(baseUrl, httpRequestInitializer);
    }

    @SneakyThrows
    public SonarIssuesResult getAllIssuesAndComponentsForProjects(List<String> projectKeys, List<String> rules) {
        GenericUrl genericUrl = createGenericUrl(projectKeys, rules);
        Set<SonarIssue> allIssues = new HashSet<>();
        Map<String, SonarComponent> allComponents = new HashMap<>();

        Long totalEffort = null;

        SonarIssueSearchResponse searchResponse;
        Long currentPage = 1L;
        Long pageSize = 500L;
        do {
            genericUrl.set("p", currentPage);
            genericUrl.set("ps", pageSize);
            HttpResponse response = httpClient.get(genericUrl);

            searchResponse = response.parseAs(SonarIssueSearchResponse.class);
            if (totalEffort == null)
                totalEffort = searchResponse.getEffortTotal();

            allIssues.addAll(searchResponse.getIssues());
            allComponents.putAll(searchResponse.getComponents().stream()
                    .collect(Collectors.toMap(SonarComponent::getKey, Function.identity())));
            currentPage++;
        } while (searchResponse.getP() * searchResponse.getPs() < searchResponse.getTotal());

        List<Issue> issues = allIssues.stream()
                .map(sonarIssue -> Issue.builder().rule(sonarIssue.getRule())
                        .project(sonarIssue.getProject())
                        .component(getComponent(allComponents, sonarIssue))
                        .effort(getEffort(sonarIssue.getEffort()))
                        .creationDate(getDate(sonarIssue.getCreationDate()))
                        .closeDate(getDate(sonarIssue.getCloseDate())).build()).collect(Collectors.toList());

        return new SonarIssuesResult(totalEffort != null ? totalEffort : 0, issues);
    }

    private GenericUrl createGenericUrl(List<String> projectKeys, List<String> rules) {
        GenericUrl genericUrl = new GenericUrl(pathResolver.getApiPath("issues", "search"));
        if (!CollectionUtils.isEmpty(projectKeys))
            genericUrl.put("componentKeys", String.join(",", projectKeys));
        if (!CollectionUtils.isEmpty(rules))
            genericUrl.put("rules", String.join(",", rules));
        return genericUrl;
    }

    Long getEffort(String effort) {
        if (effort == null) return 0L;

        Pattern pattern = Pattern.compile("\\d+[a-zA-Z]+");
        Matcher matcher = pattern.matcher(effort);

        long totalEffortInMinutes = 0;

        while (matcher.find()) {
            String group = matcher.group().trim();
            totalEffortInMinutes += getValueInMinutes(effort, group);
        }

        return totalEffortInMinutes;
    }

    Integer getValueInMinutes(String effort, String group) {
        return UNITS_TO_VALUES.entrySet().stream()
                .filter(entry -> group.endsWith(entry.getKey()))
                .findFirst()
                .map(entry -> getIntegerValue(group, entry) * entry.getValue())
                .orElseGet(() -> {
                    System.out.println("WARNING: unknown time measure " + group + " in " + effort);
                    return 0;
                });
    }

    private int getIntegerValue(String group, Map.Entry<String, Integer> entry) {
        return Integer.parseInt(group.substring(0, group.length() - entry.getKey().length()));
    }

    private ZonedDateTime getDate(String creationDate) {
        if (creationDate == null || Data.isNull(creationDate))
            return null;
        return ZonedDateTime.parse(creationDate, dateFormatter);
    }

    private Component getComponent(Map<String, SonarComponent> allComponents, SonarIssue sonarIssue) {
        SonarComponent sonarComponent = allComponents.get(sonarIssue.getComponent());
        return Component.builder().key(sonarComponent.getKey()).path(sonarComponent.getPath()).build();
    }
}
