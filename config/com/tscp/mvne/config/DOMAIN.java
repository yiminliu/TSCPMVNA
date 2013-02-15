package com.tscp.mvne.config;

import com.tscp.mvne.exception.InitializationException;

public class DOMAIN extends CONFIG {
	public static String urlSupport;
	public static String urlManage;
	public static String urlTOS;

	public static final void init() throws InitializationException {
		CONFIG.loadAll();
		try {
			urlSupport = props.getProperty("wotg.url.support");
			urlManage = props.getProperty("wotg.url.account");
			urlTOS = props.getProperty("wotg.url.tos");
		} catch (Exception e) {
			e.printStackTrace();
			throw new InitializationException(e);
		}
	}

}
