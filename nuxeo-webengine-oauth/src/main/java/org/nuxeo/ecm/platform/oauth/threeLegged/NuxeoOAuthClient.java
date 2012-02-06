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
 *     Gallouin Arthur
 *     Laurent Doguin
 */

package org.nuxeo.ecm.platform.oauth.threeLegged;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.oauth.consumers.NuxeoOAuthConsumer;
import org.nuxeo.ecm.platform.oauth.providers.NuxeoOAuthServiceProvider;
import org.nuxeo.ecm.platform.oauth.providers.OAuthServiceProviderRegistry;
import org.nuxeo.ecm.platform.oauth.providers.OAuthServiceProviderRegistryImpl;
import org.nuxeo.ecm.platform.oauth.tokens.NuxeoOAuthToken;
import org.nuxeo.ecm.platform.oauth.tokens.OAuthTokenStore;
import org.nuxeo.runtime.api.Framework;

/**
 * This class should be used to handle three legged oauth authentication. It
 * will stores the necessary properties needed during the different auth pahse.
 *
 * @author agallouin
 * @author ldoguin
 *
 */
public class NuxeoOAuthClient {

    private final static String PREFERRED_ENCODING = "UTF-8";

    public Properties props;

    private NuxeoOAuthServiceProvider nuxeoServiceProvider;

    protected static final Log log = LogFactory.getLog(OAuthServiceProviderRegistryImpl.class);

    public NuxeoOAuthClient(NuxeoOAuthServiceProvider nuxeoServiceProvider,
            String callbackUrl) throws IllegalArgumentException {
        this.nuxeoServiceProvider = nuxeoServiceProvider;
        if (nuxeoServiceProvider == null) {
            throw new IllegalArgumentException("Given Service Provider is null");
        }

        props = new Properties();
        props.setProperty("oauth_callback", callbackUrl);
        props.setProperty("consumerKey", nuxeoServiceProvider.getConsumerKey());
        props.setProperty("consumerSecret",
                nuxeoServiceProvider.getConsumerSecret());

        props.setProperty("requestUrl",
                nuxeoServiceProvider.getRequestTokenUR());
        props.setProperty("authorizationUrl",
                nuxeoServiceProvider.getUserAuthorizationURL());
        props.setProperty("accessUrl", nuxeoServiceProvider.getAccessTokenURL());
    }

    public NuxeoOAuthClient(String serviceProviderURL,
            String serviceProviderName, String callbackUrl)
            throws IllegalArgumentException {
        OAuthServiceProviderRegistry registry;
        try {
            registry = Framework.getService(OAuthServiceProviderRegistry.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not find OAuthServiceProviderRegistry service.", e);
        }
        nuxeoServiceProvider = registry.getProvider(serviceProviderURL,
                serviceProviderName);
        if (nuxeoServiceProvider == null) {
            throw new IllegalArgumentException(serviceProviderName
                    + " Service Provider doesn not exist.");
        }

        props = new Properties();
        props.setProperty("oauth_callback", callbackUrl);
        props.setProperty("consumerKey", nuxeoServiceProvider.getConsumerKey());
        props.setProperty("consumerSecret",
                nuxeoServiceProvider.getConsumerSecret());

        props.setProperty("requestUrl",
                nuxeoServiceProvider.getRequestTokenUR());
        props.setProperty("authorizationUrl",
                nuxeoServiceProvider.getUserAuthorizationURL());
        props.setProperty("accessUrl", nuxeoServiceProvider.getAccessTokenURL());
    }

    /**
     * The accessor contains necessary info to sign an OAuth request. It's
     * usually used when creating a new OAuthClient.
     *
     * @param callbackUrl
     * @param gadgetUrl
     * @param serviceProviderName
     * @param userName
     * @return
     * @throws Exception
     */
    public static OAuthAccessor buildSigningAccessor(String callbackUrl, String serviceProviderURL,
            String serviceProviderName, String userName) throws Exception {
        OAuthServiceProviderRegistry registry = Framework.getService(OAuthServiceProviderRegistry.class);
        NuxeoOAuthServiceProvider nuxeoServiceProvider = registry.getProvider(
                serviceProviderURL, serviceProviderName);
        OAuthTokenStore store = Framework.getService(OAuthTokenStore.class);
        NuxeoOAuthToken token = store.getClientAccessToken(serviceProviderName,
                userName);
        OAuthConsumer consumer = new NuxeoOAuthConsumer(callbackUrl,
                nuxeoServiceProvider.getConsumerKey(),
                nuxeoServiceProvider.getConsumerSecret(), nuxeoServiceProvider);
        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.accessToken = token.getToken();
        accessor.tokenSecret = token.getTokenSecret();
        return accessor;
    }
    /**
     *
     * @return the authorization url and append a query string containing the
     *         oauth_token and the oauth_callback if not empty. Default
     *         parameter encoding is UTF-8.
     * @throws UnsupportedEncodingException
     */
    public String createUserAuthorizationUrl()
            throws UnsupportedEncodingException {
        String url = props.getProperty("authorizationUrl") + "?";

        url = url
                + "oauth_token="
                + URLEncoder.encode(props.getProperty("requestToken"),
                        PREFERRED_ENCODING);
        if (props.getProperty("oauth_callback") != null)
            url = url
                    + "&oauth_callback="
                    + URLEncoder.encode(props.getProperty("oauth_callback"),
                            PREFERRED_ENCODING);

        return url;
    }

    /**
     * Use {@link OAuthTokenStore} service to store the token using the
     * consumerKey, oauth_callback, accessToken, tokenSecret and serviceProvider
     * name properties.
     *
     * @param User login
     */
    public void storeAccesToken(String login) {
        OAuthTokenStore store;
        try {
            store = Framework.getService(OAuthTokenStore.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not find OAuthTokenStore service.", e);
        }
        store.storeClientAccessToken(props.getProperty("consumerKey"),
                props.getProperty("oauth_callback"),
                props.getProperty("accessToken"),
                props.getProperty("tokenSecret"),
                nuxeoServiceProvider.getServiceName(), login);

    }

    /**
     * Execute the request token request. It's the first request you have to do
     * during the three legged authentication. You can pass the oauth_callback
     * parameter if needed. It will set the requestToken and tokenSecret
     * properties.
     *
     * @throws OAuthException
     * @throws URISyntaxException
     */
    public void executeRequest() throws OAuthException, URISyntaxException {
        OAuthAccessor accessor = createOAuthAccessor();

        OAuthClient client = new OAuthClient(new URLConnectionClient());
        try {
            List<OAuth.Parameter> parameters = OAuth.newList("oauth_callback",
                    props.getProperty("oauth_callback"));
            client.getRequestToken(accessor, null, parameters);
        } catch (IOException e) {
            log.error(
                    "Unable to contact the Request Token Client at "
                            + props.getProperty("requestUrl"), e);
        }

        props.setProperty("requestToken", accessor.requestToken);
        props.setProperty("tokenSecret", accessor.tokenSecret);
    }

    /**
     * Throw a {@link NuxeoOAuthClientException} containing the authorization
     * url and the current {@link NuxeoOAuthClient}.
     *
     * @throws NuxeoOAuthClientException
     * @throws UnsupportedEncodingException
     */
    public void executeAuthorizeSend() throws NuxeoOAuthClientException,
            UnsupportedEncodingException {
        String url = createUserAuthorizationUrl();
        throw NuxeoOAuthClientException.authRequired(url, this);
    }

    /**
     * Set the accessToken and oauth_verifier properties. You need them before
     * calling the {@link #executeAccess()} method. This is sually called when
     * the targeted service provider doesn't support callback.
     *
     * @param accessToken
     * @param verifier
     * @throws InterruptedException
     * @throws IOException
     */
    public void setOAuthVerfierAndAccessToken(String accessToken,
            String verifier) throws InterruptedException, IOException {
        props.setProperty("accessToken", accessToken);
        if (verifier != null)
            props.setProperty("oauth_verifier", verifier);
    }

    /**
     * Execute the Access request. This is the last request you have to do
     * during the three legged authentication process. You need the oauth_token
     * and oauth_verifier properties. It will update the oauth_token property
     * and set the oauth_token_secret property that you need to store in Nuxeo.
     *
     * @throws URISyntaxException
     * @throws OAuthException
     */
    public void executeAccess() throws URISyntaxException, OAuthException {
        Properties paramProps = new Properties();
        paramProps.setProperty("oauth_token", props.getProperty("requestToken"));
        paramProps.setProperty("oauth_verifier",
                props.getProperty("oauth_verifier"));

        OAuthMessage response;
        try {
            response = sendRequest(paramProps, props.getProperty("accessUrl"));
            props.setProperty("accessToken",
                    response.getParameter("oauth_token"));
            props.setProperty("tokenSecret",
                    response.getParameter("oauth_token_secret"));
        } catch (IOException e) {
            log.error(
                    "Unable to contact the Access Token Client at "
                            + props.getProperty("accessUrl"), e);
        }
    }

    private OAuthMessage sendRequest(Map map, String url) throws IOException,
            URISyntaxException, OAuthException {
        final List<OAuth.Parameter> parameters = OAuth.newList();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry p = (Map.Entry) it.next();
            parameters.add(new OAuth.Parameter((String) p.getKey(),
                    (String) p.getValue()));
        }
        OAuthAccessor accessor = createOAuthAccessor();
        accessor.tokenSecret = props.getProperty("tokenSecret");
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        return client.invoke(accessor, "GET", url, parameters);
    }

    private OAuthAccessor createOAuthAccessor() {
        String consumerKey = props.getProperty("consumerKey");
        String callbackUrl = props.getProperty("oauth_callback");
        String consumerSecret = props.getProperty("consumerSecret");

        String reqUrl = props.getProperty("requestUrl");
        String authzUrl = props.getProperty("authorizationUrl");
        String accessUrl = props.getProperty("accessUrl");

        OAuthServiceProvider provider = new OAuthServiceProvider(reqUrl,
                authzUrl, accessUrl);
        OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey,
                consumerSecret, provider);
        return new OAuthAccessor(consumer);
    }

}
