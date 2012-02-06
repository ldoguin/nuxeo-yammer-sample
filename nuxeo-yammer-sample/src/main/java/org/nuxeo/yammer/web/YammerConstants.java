/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     ldoguin
 */

package org.nuxeo.yammer.web;

import org.nuxeo.ecm.platform.ui.web.util.BaseURL;


public class YammerConstants {

	public static final String GADGET_URL = "https://www.yammer.com/api/v1/messages.json";

	public static final String URL_ENCODED_GADGET_URL = "https%3A%2F%2Fwww.yammer.com%2Fapi%2Fv1%2Fmessages.json";

	public static final String YAMMER_SERVICE_PROVIDER_NAME = "Yammer_service_provider";

	public static final String YAMMER_REQUEST_TOKEN_URL = "https://www.yammer.com/oauth/request_token ";

	public static final String YAMMER_AUTHORIZE_TOKEN_URL = "https://www.yammer.com/oauth/authorize";

	public static final String YAMMER_ACCESS_TOKEN_URL = "https://www.yammer.com/oauth/access_token";

	public static final String YAMMER_MESSAGE_POST_URL = "https://www.yammer.com/api/v1/messages.json";

	public static final String YAMMER_MESSAGE_BODY_PROPERTY_NAME = "body";

	public static final String YAMMER_OPEN_GRAPH_TITLE_PROPERTY_NAME = "og_title";

	public static final String YAMMER_OPEN_GRAPH_DESCRIPTION_PROPERTY_NAME = "og_description";

	public static final String YAMMER_OPEN_GRAPH_TYPE_PROPERTY_NAME = "og_type";

	public static final String YAMMER_OPEN_GRAPH_TYPE_DOCUMENT_PROPERTY_NAME = "document";

	public static final String YAMMER_OPEN_GRAPH_URL_PROPERTY_NAME = "og_url";

	public static String getSubscriberURL() {
		return BaseURL.getContextPath()
				+ String.format(
						"/site/oauthsubscriber?servicename=%s&serviceurl=%s",
						YAMMER_SERVICE_PROVIDER_NAME, URL_ENCODED_GADGET_URL);
	}
}
