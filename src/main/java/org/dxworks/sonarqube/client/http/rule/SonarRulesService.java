package org.dxworks.sonarqube.client.http.rule;

import com.google.api.client.http.HttpRequestInitializer;
import lombok.SneakyThrows;
import org.dxworks.sonarqube.client.http.SonarService;
import org.dxworks.sonarqube.client.http.rule.dto.Rule;
import org.dxworks.sonarqube.client.http.rule.dto.RuleResponse;
import org.dxworks.utils.java.rest.client.response.HttpResponse;

import java.util.Optional;

public class SonarRulesService extends SonarService {
    public SonarRulesService(String baseUrl) {
        super(baseUrl);
    }

    public SonarRulesService(String baseUrl, HttpRequestInitializer httpRequestInitializer) {
        super(baseUrl, httpRequestInitializer);
    }

    public Optional<Rule> getRule(String ruleKey) {
        RuleResponse ruleResponse = searchForRules(ruleKey);
        return ruleResponse.getRules().stream().filter(rule -> rule.getKey().equals(ruleKey)).findFirst();
    }

    @SneakyThrows
    public RuleResponse searchForRules(String query) {
        HttpResponse httpResponse = httpClient.get(new RuleUrl(getApiPath("rules", "search"), query));
        return httpResponse.parseAs(RuleResponse.class);
    }
}
