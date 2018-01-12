package org.androidpn.server.service;

import java.util.List;

import org.androidpn.server.model.Notification;

/**
 * 提供给service层使用的Notification操作
 * 
 * @author Administrator
 *
 */
public interface NotificationService {
	void saveNotification(Notification notification);

	List<Notification> findNotificationsByUserName(String username);

	void deleteNotification(Notification notification);
}
