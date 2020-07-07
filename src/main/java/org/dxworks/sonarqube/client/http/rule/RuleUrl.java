package org.dxworks.sonarqube.client.http.rule;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;
import lombok.Data;

@Data
public class RuleUrl extends GenericUrl {
    @Key
    private String q;

    public RuleUrl(String encodedUrl, String query) {
        super(encodedUrl);
        this.q = query;
    }
}
