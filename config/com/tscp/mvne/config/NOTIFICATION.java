package com.tscp.mvne.config;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.InternetAddress;

import com.tscp.mvne.exception.InitializationException;
import com.tscp.mvne.notification.NotificationClient;

public class NOTIFICATION extends CONFIG {
	public static final InternetAddress from = buildFrom();
	public static final Set<InternetAddress> bccList = buildBccList();
	public static NotificationClient CLIENT;
	public static String connectionFactory;
	public static String destination;

	protected static InternetAddress buildFrom() {
		try {
			return new InternetAddress("no-reply@webonthego.com", "WebOnTheGo");
		} catch (UnsupportedEncodingException e) {
			return new InternetAddress();
		}
	}

	protected static Set<InternetAddress> buildBccList() {
		Set<InternetAddress> bccList = new HashSet<InternetAddress>();
		try {
			bccList.add(new InternetAddress("wotg_alerts@telscape.net", "WOTG Alerts"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return bccList;
	}

	public static final void init() throws InitializationException {
		CONFIG.loadAll();
		try {
			CLIENT = NotificationClient.valueOf(props.getProperty("client.name").toUpperCase());
			connectionFactory = props.getProperty("jms.factory.queue");
			destination = props.getProperty("jms.destination.queue");
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new InitializationException(e);
		}
	}
	
}
