package com.ibm.wiotp.sdk.app.config;

public class ApplicationConfigOptionsHttp {
	public boolean verify;
	
	public ApplicationConfigOptionsHttp() {}
	
	public ApplicationConfigOptionsHttp(boolean verify) {
		this.verify = verify;
	}
	
	public static ApplicationConfigOptionsHttp generateFromEnv() {
		ApplicationConfigOptionsHttp http = new ApplicationConfigOptionsHttp();
		http.verify = Boolean.parseBoolean(System.getenv("WIOTP_OPTIONS_HTTP_VERIFY"));
		
		return http;
	}
}