package com.ibm.wiotp.sdk.app.config;

import com.ibm.wiotp.sdk.util.LoggerUtility;

public class ApplicationConfigAuth {
	public String key;
	public String token;
	
	public ApplicationConfigAuth() {}
	
	public ApplicationConfigAuth(String key, String token) {
		this.key = key;
		this.token = token;
	}
	
	public static ApplicationConfigAuth generateFromEnv() {
		ApplicationConfigAuth auth = new ApplicationConfigAuth();

		LoggerUtility.info("", "", "API_KEY = " + System.getenv("WIOTP_API_KEY"));
		LoggerUtility.info("", "", "API_TOKEN = " + System.getenv("WIOTP_API_TOKEN"));
	    // Also support WIOTP_API_KEY / WIOTP_API_TOKEN usage
		if (System.getenv("WIOTP_AUTH_KEY") != null) {
			auth.key = System.getenv("WIOTP_AUTH_KEY");
			auth.token = System.getenv("WIOTP_AUTH_TOKEN");
		} else {
			
			if (System.getenv("WIOTP_API_KEY") == null || System.getenv("WIOTP_API_TOKEN") == null) {
				throw new NullPointerException("WIOTP_AUTH_KEY/WIOTP_AUTH_TOKEN or WIOTP_API_KEY/WIOTP_API_TOKEN environment variables must be defined");
			}
			auth.key = System.getenv("WIOTP_API_KEY");
			auth.token = System.getenv("WIOTP_API_TOKEN");
		}
		
		return auth;
	}
}
