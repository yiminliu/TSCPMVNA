package com.tscp.mvne.notification;

import com.tscp.mvne.config.NOTIFICATION;
import com.tscp.mvne.jms.AbstractQueueSender;

public class NotificationSender extends AbstractQueueSender {
	public static final String QUEUE_USERNAME = "guest";
	public static final String QUEUE_PASSWORD = "guest";

	public NotificationSender() {
		setConnectionFactory(NOTIFICATION.connectionFactory);
		setDestination(NOTIFICATION.destination);
	}

}