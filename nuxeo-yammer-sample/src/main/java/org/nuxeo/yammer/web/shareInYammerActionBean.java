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

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.oauth.threeLegged.NuxeoOAuthClient;
import org.nuxeo.ecm.platform.oauth.threeLegged.NuxeoOAuthClientException;
import org.nuxeo.ecm.platform.oauth.tokens.NuxeoOAuthToken;
import org.nuxeo.ecm.platform.oauth.tokens.OAuthTokenStore;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.ecm.webengine.oauth.WEOAuthConstants;
import org.nuxeo.runtime.api.Framework;

/**
 * A simple bean that creates an OAuth client and use it to post the
 * currentDocument on Yammer.
 */
@Name("shareInYammer")
@Scope(ScopeType.SESSION)
public class shareInYammerActionBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@In(create = true)
	protected NavigationContext navigationContext;

	@In(required = false)
	protected transient Principal currentUser;

	@In(create = true, required = false)
	protected FacesMessages facesMessages;

	@In(create = true)
	protected ResourcesAccessor resourcesAccessor;

	public String comment;

	public String code;

	// assume the user is authorized if he has a token
	public Boolean authorized = true;

	public Boolean hasToken;

	protected NuxeoOAuthClient client;

	public boolean hasToken() throws Exception {
		OAuthTokenStore store = Framework.getService(OAuthTokenStore.class);
		NuxeoOAuthToken token = store.getClientAccessToken(
				YammerConstants.YAMMER_SERVICE_PROVIDER_NAME,
				currentUser.getName());
		if (token == null) {
			hasToken = false;
		} else {
			hasToken = true;
		}
		return hasToken;
	}

	public void shareCurrentDocument() throws Exception {
		try {
			DocumentModel currentDocument = navigationContext
					.getCurrentDocument();
			// Create the accessor with the necessary parameters to sign the
			// request
			OAuthAccessor accessor = NuxeoOAuthClient.buildSigningAccessor(
					WEOAuthConstants.getDefaultCallbackURL(),
					YammerConstants.GADGET_URL,
					YammerConstants.YAMMER_SERVICE_PROVIDER_NAME,
					currentUser.getName());
			// Create the OAuthClient that will take care of signing the request
			// with the given accessor.
			OAuthClient client = new OAuthClient(new URLConnectionClient());
			final List<OAuth.Parameter> parameters = OAuth
					.newList(
							YammerConstants.YAMMER_MESSAGE_BODY_PROPERTY_NAME,
							comment,
							YammerConstants.YAMMER_OPEN_GRAPH_TITLE_PROPERTY_NAME,
							currentDocument.getTitle(),
							YammerConstants.YAMMER_OPEN_GRAPH_DESCRIPTION_PROPERTY_NAME,
							currentDocument.getProperty("dc:description")
									.getValue(String.class),
							YammerConstants.YAMMER_OPEN_GRAPH_TYPE_PROPERTY_NAME,
							YammerConstants.YAMMER_OPEN_GRAPH_TYPE_DOCUMENT_PROPERTY_NAME,
							YammerConstants.YAMMER_OPEN_GRAPH_URL_PROPERTY_NAME,
							DocumentModelFunctions.documentUrl(currentDocument));
			// send the request
			client.invoke(accessor, OAuthMessage.POST,
					YammerConstants.YAMMER_MESSAGE_POST_URL, parameters);
			facesMessages.add(StatusMessage.Severity.INFO, resourcesAccessor
					.getMessages().get("feedback.yammer.document.shared"));
			comment = "";
		} catch (NuxeoOAuthClientException e) {
			authorized = false;
		} catch (OAuthProblemException e1) {
			authorized = false;
		}
	}

	@Factory(value = "yammerSubscriberUrl", scope = ScopeType.SESSION)
	public String getYammerSubscriberUrl() {
		return YammerConstants.getSubscriberURL();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Boolean getAuthorized() {
		return authorized;
	}

	public void setAuthorized(Boolean authorized) {
		this.authorized = authorized;
	}
}
