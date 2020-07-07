package org.dxworks.sonarqube.client.http.rule.dto;


import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import lombok.Data;

@Data
public class Rule extends GenericJson {

    @Key
    private String key;
    @Key
    private String repo;
    @Key
    private String name;
    @Key
    private String langName;
    @Key
    private String type;

}
