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

import org.nuxeo.ecm.platform.ui.web.util.BaseURL;

public class WEOAuthConstants {

    public static final String SERVICE_PROVIDER_NAME_PARAMETER = "servicename";

    public static final String SERVICE_PROVIDER_URL_PARAMETER = "serviceurl";

    public static final String NUXEO_OAUTH_CLIENT_PARAMETER = "nuxeo_oauth_helper";

    public static String getDefaultCallbackURL() {
        return BaseURL.getContextPath() + "/site/oauthsubscriber/callback";

    }
}
