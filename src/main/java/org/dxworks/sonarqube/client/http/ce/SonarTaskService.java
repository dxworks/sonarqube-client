package org.dxworks.sonarqube.client.http.ce;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import lombok.SneakyThrows;
import org.dxworks.sonarqube.client.http.SonarService;
import org.dxworks.sonarqube.client.http.ce.dto.TaskResponse;

public class SonarTaskService extends SonarService {
    public SonarTaskService(String apiBaseUrl) {
        super(apiBaseUrl);
    }

    public SonarTaskService(String apiBaseUrl, HttpRequestInitializer httpRequestInitializer) {
        super(apiBaseUrl, httpRequestInitializer);
    }

    @SneakyThrows
    public TaskStatus getTaskStatus(String taskID) {
        HttpResponse httpResponse = httpClient.get(new TaskUrl(getApiPath("ce", "task"), taskID));
        return TaskStatus.valueOf(httpResponse.parseAs(TaskResponse.class).getStatus());
    }

}
