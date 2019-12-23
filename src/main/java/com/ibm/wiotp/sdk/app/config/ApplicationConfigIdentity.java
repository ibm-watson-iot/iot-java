package com.ibm.wiotp.sdk.app.config;

import java.util.Map;
import java.util.UUID;

public class ApplicationConfigIdentity {
	public String appId;

	public ApplicationConfigIdentity() {
		this.appId = UUID.randomUUID().toString();
	}

	public ApplicationConfigIdentity(String appId) {
		this.appId = appId;
	}

	public static ApplicationConfigIdentity generateFromEnv() {
		ApplicationConfigIdentity identity = new ApplicationConfigIdentity();
		identity.appId = System.getenv("WIOTP_IDENTITY_APPID");
		if (identity.appId == null) {
			identity.appId = UUID.randomUUID().toString();
		}

		return identity;
	}

	public static ApplicationConfigIdentity generateFromConfig(Map<String, Object> yamlIdentity) {
		ApplicationConfigIdentity identity = new ApplicationConfigIdentity();
		identity.appId = (String) yamlIdentity.get("appId");
		if (identity.appId == null) {
			identity.appId = UUID.randomUUID().toString();
		}

		return identity;
	}
}
