package org.dxworks.sonarqube.client.http;

import com.google.api.client.http.HttpRequestInitializer;
import org.dxworks.utils.java.rest.client.RestClient;

public class SonarService extends RestClient {
    public SonarService(String baseUrl) {
        super(baseUrl + "/api");
    }

    public SonarService(String baseUrl, HttpRequestInitializer httpRequestInitializer) {
        super(baseUrl + "/api", httpRequestInitializer);
    }
}
