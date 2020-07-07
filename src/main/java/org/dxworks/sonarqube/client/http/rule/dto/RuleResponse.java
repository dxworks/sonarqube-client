package org.dxworks.sonarqube.client.http.rule.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import lombok.Data;

import java.util.List;

@Data
public class RuleResponse extends GenericJson {

    @Key
    private Long p;
    @Key
    private Long ps;
    @Key
    private Long total;
    @Key
    private Long effortTotal;

    @Key
    private List<Rule> rules;
}
