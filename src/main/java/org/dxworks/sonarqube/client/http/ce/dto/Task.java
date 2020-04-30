package org.dxworks.sonarqube.client.http.ce.dto;

import com.google.api.client.util.Key;
import lombok.Data;

@Data
public class Task {
    @Key
    private String id;
    @Key
    private String status;
    @Key
    private String analysisId;
}
