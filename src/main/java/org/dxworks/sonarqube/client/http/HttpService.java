package org.dxworks.sonarqube.client.http;

import com.google.api.client.http.HttpRequestInitializer;
import org.dxworks.utils.java.rest.client.HttpClient;
import org.dxworks.utils.java.rest.client.PathResolver;


public abstract class HttpService {
	protected final PathResolver pathResolver;
	protected final HttpClient httpClient;

	public HttpService(PathResolver pathResolver) {
		this.pathResolver = pathResolver;
		this.httpClient = new HttpClient();
	}

	public HttpService(PathResolver pathResolver, HttpRequestInitializer httpRequestInitializer) {
		this.pathResolver = pathResolver;
		this.httpClient = new HttpClient(httpRequestInitializer);
	}

}
