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

package org.nuxeo.ecm.webengine.oauth;

import java.net.URI;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import net.oauth.OAuthException;

import org.nuxeo.ecm.platform.oauth.providers.NuxeoOAuthServiceProvider;
import org.nuxeo.ecm.platform.oauth.providers.OAuthServiceProviderRegistry;
import org.nuxeo.ecm.platform.oauth.threeLegged.NuxeoOAuthClient;
import org.nuxeo.ecm.platform.oauth.threeLegged.NuxeoOAuthClientException;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 * The root entry for the WebEngine module.
 *
 * @author ldoguin
 */
@Path("/oauthsubscriber")
@Produces("text/html;charset=UTF-8")
@WebObject(type = "oauthsubscriber")
public class Root extends ModuleRoot {

    /**
     *
     *
     * @param serviceProviderName
     * @param serviceProviderURL
     * @return the rendered page.
     */
    @GET
    public Object doGet(
            @QueryParam(WEOAuthConstants.SERVICE_PROVIDER_NAME_PARAMETER) String serviceProviderName,
            @QueryParam(WEOAuthConstants.SERVICE_PROVIDER_URL_PARAMETER) String serviceProviderURL) {
        OAuthServiceProviderRegistry registry = Framework.getLocalService(OAuthServiceProviderRegistry.class);
        NuxeoOAuthServiceProvider nuxeoServiceProvider = registry.getProvider(
                serviceProviderURL, serviceProviderName);
        if (nuxeoServiceProvider == null) {
            return getView("index");
        } else {
            ctx.setProperty("nsp", nuxeoServiceProvider);
            return getView("index").arg("nsp", nuxeoServiceProvider).arg(
                    "queryString", request.getQueryString()).arg("callback",
                    getCallbackUrl());
        }
    }

    @GET
    @Path("authorize")
    public Object authorize(
            @QueryParam(WEOAuthConstants.SERVICE_PROVIDER_NAME_PARAMETER) String serviceProviderName,
            @QueryParam(WEOAuthConstants.SERVICE_PROVIDER_URL_PARAMETER) String serviceProviderURL)
            throws Exception {
        OAuthServiceProviderRegistry registry = Framework.getLocalService(OAuthServiceProviderRegistry.class);
        NuxeoOAuthServiceProvider nuxeoServiceProvider = registry.getProvider(
                serviceProviderURL, serviceProviderName);
        NuxeoOAuthClient client = new NuxeoOAuthClient(nuxeoServiceProvider,
                getCallbackUrl());
        request.getSession().setAttribute(
                WEOAuthConstants.NUXEO_OAUTH_CLIENT_PARAMETER, client);
        client.executeRequest();
        try {
            client.executeAuthorizeSend();
        } catch (NuxeoOAuthClientException e) {
            return Response.seeOther(new URI(e.getAuthenticationURL())).build();
        }
        return null;
    }

    @GET
    @Path("callback")
    public Object callback(@QueryParam("oauth_token") String accessToken,
            @QueryParam("oauth_verifier") String oauth_verifier) {

        NuxeoOAuthClient client = (NuxeoOAuthClient) request.getSession().getAttribute(
                WEOAuthConstants.NUXEO_OAUTH_CLIENT_PARAMETER);
        try {
            client.setOAuthVerfierAndAccessToken(accessToken, oauth_verifier);
            client.executeAccess();
            client.storeAccesToken(getCurrentUsername());
        } catch (OAuthException e) {
            throw new RuntimeException();
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return getView("registered_token");

    }

    @POST
    @Path("enterCode")
    public Object enterCode(@FormParam("code") String code) throws Exception {
        if (code == null || "".equals(code)) {
            return "code is empty";
        }
        NuxeoOAuthClient client = (NuxeoOAuthClient) request.getSession().getAttribute(
                WEOAuthConstants.NUXEO_OAUTH_CLIENT_PARAMETER);
        client.props.put("oauth_verifier", code);
        client.executeAccess();
        client.storeAccesToken(getCurrentUsername());
        return getView("registered_token");
    }

    protected String getCurrentUsername() {
        return ctx.getPrincipal().getName();
    }

    protected String getCallbackUrl() {
        return getURL() + "/callback";
    }
}
