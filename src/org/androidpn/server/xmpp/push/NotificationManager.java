/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.androidpn.server.xmpp.push;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.androidpn.server.model.Notification;
import org.androidpn.server.model.User;
import org.androidpn.server.service.NotificationService;
import org.androidpn.server.service.ServiceLocator;
import org.androidpn.server.service.UserNotFoundException;
import org.androidpn.server.service.UserService;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;

/** 
 * This class is to manage sending the notifcations to the users.  
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationManager {

    private static final String NOTIFICATION_NAMESPACE = "androidpn:iq:notification";

    private final Log log = LogFactory.getLog(getClass());

    private SessionManager sessionManager;
    
    private NotificationService notificationService;
    
    private UserService userService;

    /**
     * Constructor.
     */
    public NotificationManager() {
        sessionManager = SessionManager.getInstance();
        notificationService = ServiceLocator.getNotificationService();
        userService = ServiceLocator.getUserService();
    }

    /**
     * Broadcasts a newly created notification message to all connected users.
     * 
     * @param apiKey the API key
     * @param title the title
     * @param message the message details
     * @param uri the uri
     */
	public void sendBroadcast(String apiKey, String title, String message, String uri) {
		/*log.debug("sendBroadcast()...");
		IQ notificationIQ = createNotificationIQ(apiKey, title, message, uri);
		for (ClientSession session : sessionManager.getSessions()) {
			if (session.getPresence().isAvailable()) {
				notificationIQ.setTo(session.getAddress());
				session.deliver(notificationIQ);
			}
		}*/
	}
    
    /**
     * Broadcasts a newly created notification message to all users.
     * 
     * @param apiKey the API key
     * @param title the title
     * @param message the message details
     * @param uri the uri
     */
	public void sendBroadcastToAllUsers(String apiKey, String title, String message, String uri) {
		log.debug("sendBroadcast()...");
		List<User> allUsers = userService.getUsers();
		for (User user : allUsers) {
			Random random = new Random();
			String id = Integer.toHexString(random.nextInt());// id生成策略。数据范围很大，可以认为是惟一的。具体业务使用情况，可以再处理。
			IQ notificationIQ = createNotificationIQ(id, apiKey, title, message, uri);
			ClientSession session = sessionManager.getSession(user.getUsername());
			if (session != null && session.getPresence().isAvailable()) {
				notificationIQ.setTo(session.getAddress());
				session.deliver(notificationIQ);
			}
			saveNotification(apiKey, user.getUsername(), title, message, uri, id);
		}
	}

	/**
	 * Sends a newly created notification message to the specific user.
	 * 
	 * @param apiKey
	 * @param username
	 * @param title
	 * @param message
	 * @param uri
	 */
	public void sendNotifcationToUser(String apiKey, String username, String title, String message, String uri,
			String id_from_db) {
		log.debug("sendNotifcationToUser()...");
		Random random = new Random();
		String id;
		boolean offlineMessage = false;//是否为离线消息
		offlineMessage = (id_from_db != null) && (id_from_db.trim().length() > 0);
		if (offlineMessage) {
			id = id_from_db;
		} else {//非离线消息
			id = Integer.toHexString(random.nextInt());// id生成策略。数据范围很大，可以认为是惟一的。具体业务使用情况，可以再处理。
		}
		IQ notificationIQ = createNotificationIQ(id, apiKey, title, message, uri);
		ClientSession session = sessionManager.getSession(username);
		if (session != null) {
			if (session.getPresence().isAvailable()) {
				notificationIQ.setTo(session.getAddress());
				session.deliver(notificationIQ);
			}
		}
		if (offlineMessage) {
			//do nothing
		} else {
			//非离线消息，保存到数据库
			try {
				User user = userService.getUserByUsername(username);
				if (user != null) {
					saveNotification(apiKey, username, title, message, uri, id);
				}
			} catch (UserNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 根据用户别名来发送消息(比如应用有其他的账号体系，此时可使用该账号体系进行消息发送).
	 * 
	 * @param apiKey
	 * @param alias
	 * @param title
	 * @param message
	 * @param uri
	 * @param id_from_db
	 */
	public void sendNotificationByAlias(String apiKey, String alias, String title, String message, String uri,
			String id_from_db) {
		String username = sessionManager.getUsernameByAlias(alias);
		if (username != null) {
			sendNotifcationToUser(apiKey, username, title, message, uri, id_from_db);
		}
	}
	
	/**
	 * 通过标签发送消息
	 * 
	 * @param apiKey
	 * @param tag
	 * @param title
	 * @param message
	 * @param uri
	 * @param id_from_db
	 */
	public void sendNotificationByTag(String apiKey, String tag, String title, String message, String uri,
			String id_from_db) {
		Set<String> usernameSet = sessionManager.getUsernamesByTag(tag);
		if (usernameSet != null && !usernameSet.isEmpty()) {
			for (String username : usernameSet) {
				sendNotifcationToUser(apiKey, username, title, message, uri, id_from_db);
			}
		}
	}

	/**
	 * 将Notification保存到服务器端数据库
	 */
	private void saveNotification(String apiKey, String username, String title, String message, String uri,
			String uuid) {
		Notification notification = new Notification();
		notification.setApiKey(apiKey);
		notification.setUsername(username);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setUri(uri);
		notification.setUuid(uuid);
		notificationService.saveNotification(notification);
	}
    
	/**
	 * Creates a new notification IQ and returns it.
	 */
	private IQ createNotificationIQ(String id, String apiKey, String title, String message, String uri) {
		// String id = String.valueOf(System.currentTimeMillis());

		Element notification = DocumentHelper.createElement(QName.get("notification", NOTIFICATION_NAMESPACE));
		notification.addElement("id").setText(id);
		notification.addElement("apiKey").setText(apiKey);
		notification.addElement("title").setText(title);
		notification.addElement("message").setText(message);
		notification.addElement("uri").setText(uri);

		IQ iq = new IQ();
		iq.setType(IQ.Type.set);
		iq.setChildElement(notification);

		return iq;
	}
}
