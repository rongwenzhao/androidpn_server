package org.androidpn.server.dao;

import java.util.List;

import org.androidpn.server.model.Notification;

/**
 * Notification的操作接口
 * @author Administrator
 *
 */
public interface NotificationDao {
	void saveNotification(Notification notification);

	List<Notification> findNotificationsByUserName(String username);

	void deleteNotification(Notification notification);
}
