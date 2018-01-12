package org.androidpn.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 服务器端发送离线消息需要的实体类以及其数据库表映射配置；
 * 该处配置之后，在任何数据库中都可使用，只需要修改jdbc.properties文件中hibernate.dialect属性即可。
 * 
 * @author Administrator
 */

@Entity
@Table(name = "notification")
public class Notification implements Serializable {
	private static final long serialVersionUID = -8185019956659917022L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	// 消息推送给谁, unique = false 因为一个用户不一定只有一个消息需要推送
	@Column(name = "username", nullable = false, length = 64)
	private String username;
	// 消息的标题，非空
	@Column(name = "title", nullable = false, length = 64)
	private String title;
	// 消息内容，非空
	@Column(name = "message", nullable = false, length = 1000)
	private String message;
	// apiKey，商用时需要使用
	@Column(name = "api_key", length = 64)
	private String apiKey;
	// 消息的连接，点击相应按钮跳转到的消息补充内容
	@Column(name = "uri", length = 256)
	private String uri;
	// 消息的唯一标志符。用于推送消息，以及客户端回执时的消息唯一标志。
	@Column(name = "uuid", length = 64, nullable = false, unique = true)
	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
