package com.tscp.mvne.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tscp.mvne.exception.InitializationException;

public class CONFIG {
	public static final String configFile = "com/tscp/mvne/config/config.properties";
	public static final String connectionFile = "com/tscp/mvne/config/service.properties";
	public static final String deviceFile = "com/tscp/mvne/config/device.properties";
	public static final String domainFile = "com/tscp/mvne/config/domain.properties";
	public static final String provisionFile = "com/tscp/mvne/config/provision.properties";
	public static final String billingFile = "com/tscp/mvne/config/billing.properties";
	public static final String notificationFile = "com/tscp/mvne/config/notification.properties";
	protected static final Logger logger = LoggerFactory.getLogger("TSCPMVNA");
	protected static Set<String> loadedFiles = new HashSet<String>();
	protected static Properties props = new Properties();
	protected static boolean production = false;

	public static void init() throws InitializationException {
		logger.info("--> INITIALIZING CONFIGURATION");
		load(configFile);
		if (props.getProperty("production") != null && props.getProperty("production").equals("1")) {
			production = true;
		} else {
			production = false;
		}
	}

	public static void initAll() throws InitializationException {
		CONFIG.init();
		CONNECTION.init();
		DEVICE.init();
		DOMAIN.init();
		PROVISION.init();
		BILLING.init();
		NOTIFICATION.init();
	}

	protected static void loadAll() throws InitializationException {
		load(configFile);
		load(deviceFile);
		load(domainFile);
		load(provisionFile);
		load(billingFile);
		load(connectionFile);
		load(notificationFile);
	}

	protected static void load(String filepath) throws InitializationException {
		if (!loadedFiles.contains(filepath)) {
			try {
				logger.info("LOADING PROPERTIES: {}", filepath);
				props.load(CONFIG.class.getClassLoader().getResourceAsStream(filepath));
				loadedFiles.add(filepath);
			} catch (IOException e) {
				logger.error("UNABLE TO LOAD PROPERTIES FILE: {}", filepath);
				throw new InitializationException(e);
			}
		}
	}

}