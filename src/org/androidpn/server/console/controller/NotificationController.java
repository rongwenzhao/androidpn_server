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
package org.androidpn.server.console.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.androidpn.server.util.Config;
import org.androidpn.server.xmpp.push.NotificationManager;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * A controller class to process the notification related requests.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationController extends MultiActionController {

	private NotificationManager notificationManager;

	public NotificationController() {
		notificationManager = new NotificationManager();
	}

	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		// mav.addObject("list", null);
		mav.setViewName("notification/form");
		return mav;
	}

	public ModelAndView send(HttpServletRequest request, HttpServletResponse response) throws Exception {
		/*
		 * String broadcast = ServletRequestUtils.getStringParameter(request,
		 * "broadcast", "0"); String username =
		 * ServletRequestUtils.getStringParameter(request, "username"); String alias =
		 * ServletRequestUtils.getStringParameter(request, "alias"); String tag =
		 * ServletRequestUtils.getStringParameter(request, "tag"); String title =
		 * ServletRequestUtils.getStringParameter(request, "title"); String message =
		 * ServletRequestUtils.getStringParameter(request, "message"); String uri =
		 * ServletRequestUtils.getStringParameter(request, "uri");
		 */

		String broadcast = null;
		String username = null;
		String alias = null;
		String tag = null;
		String title = null;
		String message = null;
		String uri = null;
		String imageUrl = null;

		String apiKey = Config.getString("apiKey", "");
		logger.debug("apiKey=" + apiKey);

		// 用的 commons-fileupload jar包实现的文件上传。此时form.jsp使用的enctype="multipart/form-data"
		// ，ServletRequestUtils获取不到页面的值传递。
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
		List<FileItem> fileItemList = servletFileUpload.parseRequest(request);
		for (FileItem fileItem : fileItemList) {
			if ("broadcast".equals(fileItem.getFieldName())) {
				broadcast = fileItem.getString("utf-8");
			} else if ("username".equals(fileItem.getFieldName())) {
				username = fileItem.getString("utf-8");
			} else if ("alias".equals(fileItem.getFieldName())) {
				alias = fileItem.getString("utf-8");
			} else if ("tag".equals(fileItem.getFieldName())) {
				tag = fileItem.getString("utf-8");
			} else if ("title".equals(fileItem.getFieldName())) {
				title = fileItem.getString("utf-8");
			} else if ("message".equals(fileItem.getFieldName())) {
				message = fileItem.getString("utf-8");
			} else if ("uri".equals(fileItem.getFieldName())) {
				uri = fileItem.getString("utf-8");
			} else if ("image".equals(fileItem.getFieldName())) {
				imageUrl = uploadImage(request, fileItem);
			}
		}

		if ("0".equalsIgnoreCase(broadcast)) {
			// 广播
			notificationManager.sendBroadcastToAllUsers(apiKey, title, message, uri, imageUrl);
		} else if ("1".equalsIgnoreCase(broadcast)) {
			// 通过用户名发送
			notificationManager.sendNotifcationToUser(apiKey, username, title, message, uri, imageUrl, null);
		} else if ("2".equalsIgnoreCase(broadcast)) {
			// 通过别名发送
			notificationManager.sendNotificationByAlias(apiKey, alias, title, message, uri, imageUrl, null);
		} else if ("3".equalsIgnoreCase(broadcast)) {
			// 通过标签发送。发送给一组用户
			notificationManager.sendNotificationByTag(apiKey, tag, title, message, uri, imageUrl, null);
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName("redirect:notification.do");
		return mav;
	}

	/**
	 * 文件上传方法
	 * 
	 * @param request
	 * @param fileItem
	 * @return
	 * @throws Exception
	 */
	private String uploadImage(HttpServletRequest request, FileItem fileItem) throws Exception {
		String uploadPath = getServletContext().getRealPath("/upload");
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}
		if (fileItem != null && fileItem.getContentType().startsWith("image")) {// 只处理图片类型
			String suffix = fileItem.getName().substring(fileItem.getName().indexOf("."));
			// 上传的图片名字的生成策略，对于比较多并发的情况，可以优化，更换生成策略，不然会有文件名重复的风险。
			String fileName = System.currentTimeMillis() + suffix;
			InputStream is = fileItem.getInputStream();
			FileOutputStream fos = new FileOutputStream(uploadDir + "/" + fileName);
			byte[] bytes = new byte[1024];
			int len = 0;
			while ((len = is.read(bytes)) > 0) {
				fos.write(bytes, 0, len);
				fos.flush();
			}
			fos.close();
			is.close();
			String serverName = request.getServerName();
			int serverPort = request.getServerPort();
			String imageUrl = "http://" + "192.168.0.102" + ":" + serverPort + "/upload/" + fileName;
			System.out.println("imageUrl = " + imageUrl);
			return imageUrl;
		}
		return "";
	}

}
