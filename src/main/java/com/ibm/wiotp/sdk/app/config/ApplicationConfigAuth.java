package com.ibm.wiotp.sdk.app.config;

import java.util.Map;

public class ApplicationConfigAuth {
	public String key;
	public String token;

	public ApplicationConfigAuth() {
	}

	public ApplicationConfigAuth(String key, String token) {
		this.key = key;
		this.token = token;
	}

	public static ApplicationConfigAuth generateFromEnv() {
		ApplicationConfigAuth auth = new ApplicationConfigAuth();

		// Also support WIOTP_API_KEY / WIOTP_API_TOKEN usage
		if (System.getenv("WIOTP_AUTH_KEY") != null) {
			auth.key = System.getenv("WIOTP_AUTH_KEY");
			auth.token = System.getenv("WIOTP_AUTH_TOKEN");
		} else {

			if (System.getenv("WIOTP_API_KEY") == null || System.getenv("WIOTP_API_TOKEN") == null) {
				throw new NullPointerException(
						"WIOTP_AUTH_KEY/WIOTP_AUTH_TOKEN or WIOTP_API_KEY/WIOTP_API_TOKEN environment variables must be defined");
			}
			auth.key = System.getenv("WIOTP_API_KEY");
			auth.token = System.getenv("WIOTP_API_TOKEN");
		}

		return auth;
	}

	public static ApplicationConfigAuth generateFromConfig(Map<String, Object> yamlAuth) {
		ApplicationConfigAuth auth = new ApplicationConfigAuth();
		auth.key = (String) yamlAuth.get("key");
		auth.token = (String) yamlAuth.get("token");

		return auth;
	}
}
