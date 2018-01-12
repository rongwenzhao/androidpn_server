package org.androidpn.server.xmpp.handler;

import org.androidpn.server.service.NotificationService;
import org.androidpn.server.service.ServiceLocator;
import org.androidpn.server.xmpp.UnauthorizedException;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

/**
 * 处理回执消息的handler
 * 
 * @author Administrator
 *
 */
public class IQDeliverConfirmHandler extends IQHandler {
	private static final String NAMESPACE = "androidpn:iq:deliverconfirm";

	private NotificationService notificationService;

	public IQDeliverConfirmHandler() {
		notificationService = ServiceLocator.getNotificationService();
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ reply = null;

		ClientSession session = sessionManager.getSession(packet.getFrom());
		// 保护措施
		if (session == null) {
			log.error("Session not found for key " + packet.getFrom());
			reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setError(PacketError.Condition.internal_server_error);
			return reply;
		}

		if (session.getStatus() == Session.STATUS_AUTHENTICATED) {// 防止伪造请求的发生
			if (IQ.Type.set.equals(packet.getType())) {
				// 合法的消息回执
				Element element = packet.getChildElement();
				String uuid = element.elementText("uuid");
				// 根据客户端返回的uuid，服务器删除掉数据库中对应uuid的消息。
				notificationService.deleteNotificationByUUID(uuid);
			}
		}
		return null;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

}
