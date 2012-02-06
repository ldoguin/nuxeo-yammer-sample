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
 */

package org.nuxeo.ecm.platform.oauth.threeLegged;

import org.nuxeo.ecm.core.api.ClientException;

public class NuxeoOAuthClientException extends ClientException {

    private static final long serialVersionUID = 1L;

    protected boolean oauthError = false;

    protected String authenticationURL = null;

    protected NuxeoOAuthClient client = null;

    public NuxeoOAuthClientException() {
        super();
    }

    public NuxeoOAuthClientException(Exception e) {
        super(e);
    }

    public static NuxeoOAuthClientException authRequired(String url,
            NuxeoOAuthClient client) {
        NuxeoOAuthClientException ex = new NuxeoOAuthClientException();
        ex.oauthError = true;
        ex.authenticationURL = url;
        ex.client = client;
        return ex;
    }

    public boolean isOauthError() {
        return oauthError;
    }

    public String getAuthenticationURL() {
        return authenticationURL;
    }

    public NuxeoOAuthClient getClient() {
        return client;
    }

}
