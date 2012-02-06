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

package org.nuxeo.yammer.operation;

import java.util.List;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.oauth.threeLegged.NuxeoOAuthClient;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.webengine.oauth.WEOAuthConstants;
import org.nuxeo.yammer.web.YammerConstants;

/**
 * @author ldoguin
 */
@Operation(id = ShareInYammer.ID, category = Constants.CAT_DOCUMENT, label = "ShareInYammer", description = "This operation will post the given document and comment on yammer")
public class ShareInYammer {

	public static final String ID = "ShareInYammer";

	@Context
	public CoreSession coreSession;

	@Param(name = "document")
	public DocumentModel doc;

	@Param(name = "comment")
	public String comment;

	@OperationMethod
	public void run() throws Exception {
		OAuthAccessor accessor = NuxeoOAuthClient.buildSigningAccessor(
				WEOAuthConstants.getDefaultCallbackURL(),
				YammerConstants.GADGET_URL,
				YammerConstants.YAMMER_SERVICE_PROVIDER_NAME,
				coreSession.getPrincipal().getName());
		OAuthClient client = new OAuthClient(new URLConnectionClient());
		final List<OAuth.Parameter> parameters = OAuth.newList(
				YammerConstants.YAMMER_MESSAGE_BODY_PROPERTY_NAME, comment,
				YammerConstants.YAMMER_OPEN_GRAPH_TITLE_PROPERTY_NAME, doc.getTitle(),
				YammerConstants.YAMMER_OPEN_GRAPH_DESCRIPTION_PROPERTY_NAME,
				doc.getProperty("dc:description").getValue(String.class),
				YammerConstants.YAMMER_OPEN_GRAPH_TYPE_PROPERTY_NAME,
				YammerConstants.YAMMER_OPEN_GRAPH_TYPE_DOCUMENT_PROPERTY_NAME,
				YammerConstants.YAMMER_OPEN_GRAPH_URL_PROPERTY_NAME,
				DocumentModelFunctions.documentUrl(doc));

		client.invoke(accessor, OAuthMessage.POST,
				YammerConstants.YAMMER_MESSAGE_POST_URL, parameters);
	}

}
