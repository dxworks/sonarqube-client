package org.dxworks.sonarqube.client.http.ce.dto;

import com.google.api.client.util.Key;
import lombok.Data;
import lombok.experimental.Delegate;

@Data
public class TaskResponse {
    @Key
    @Delegate
    private Task task;
}
