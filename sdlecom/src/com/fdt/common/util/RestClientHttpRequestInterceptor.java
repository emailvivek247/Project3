package com.fdt.common.util;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

/**
 * This class is an Intercepter, that inject the UserName and Password to the Web Services
 *
 */
public class RestClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	@Value("${webservice.username}")
	private String userName = null;

	@Value("${webservice.password}")
	private String password = null;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
		throws IOException {
		HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
		requestWrapper.getHeaders().add("USERNAME", userName);
		requestWrapper.getHeaders().add("PASSWORD", password);
		return execution.execute(requestWrapper, body);
	}
}