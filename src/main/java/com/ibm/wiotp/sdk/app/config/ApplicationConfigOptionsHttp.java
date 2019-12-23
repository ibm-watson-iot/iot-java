package com.ibm.wiotp.sdk.app.config;

import java.util.Map;

public class ApplicationConfigOptionsHttp {
	public boolean verify;

	public ApplicationConfigOptionsHttp() {
	}

	public ApplicationConfigOptionsHttp(boolean verify) {
		this.verify = verify;
	}

	public static ApplicationConfigOptionsHttp generateFromEnv() {
		ApplicationConfigOptionsHttp http = new ApplicationConfigOptionsHttp();
		http.verify = Boolean.parseBoolean(System.getenv("WIOTP_OPTIONS_HTTP_VERIFY"));

		return http;
	}

	public static ApplicationConfigOptionsHttp generateFromConfig(Map<String, Object> yamlHttp) {
		ApplicationConfigOptionsHttp http = new ApplicationConfigOptionsHttp();
		http.verify = Boolean.parseBoolean((String) yamlHttp.get("verify"));

		return http;
	}
}