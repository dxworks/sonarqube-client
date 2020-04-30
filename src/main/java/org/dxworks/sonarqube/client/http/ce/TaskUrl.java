package org.dxworks.sonarqube.client.http.ce;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class TaskUrl extends GenericUrl {
    @Key
    private String id;

    public TaskUrl(String encodedUrl, String id) {
        super(encodedUrl);
        this.id = id;
    }
}
