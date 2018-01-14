package org.androidpn.server.xmpp.handler;

import org.androidpn.server.xmpp.UnauthorizedException;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

/**
 * 处理设置别名的handler
 * 
 * @author Administrator
 *
 */
public class IQSetAliasHandler extends IQHandler {
	private static final String NAMESPACE = "androidpn:iq:setalias";

	public IQSetAliasHandler() {
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
				// 设置用户名及对应别名
				Element element = packet.getChildElement();
				String username = element.elementText("username");
				String alias = element.elementText("alias");
				if (username != null && !username.equals("") && alias != null && !alias.equals("")) {
					sessionManager.setAliasUsername(username, alias);
					System.out.println("alias set successfully=====");
				}
			}
		}
		return null;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

}
