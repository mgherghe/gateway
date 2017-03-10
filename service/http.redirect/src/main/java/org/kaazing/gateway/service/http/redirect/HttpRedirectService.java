/**
 * Copyright 2007-2016, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaazing.gateway.service.http.redirect;

import org.apache.mina.core.session.IoSession;
import org.kaazing.gateway.service.Service;
import org.kaazing.gateway.service.ServiceContext;
import org.kaazing.gateway.service.ServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gateway service of type "redirect".
 */
public class HttpRedirectService implements Service {

    private final Logger logger = LoggerFactory.getLogger("service.http.redirect");

    private HttpRedirectServiceHandler handler;
    private ServiceContext serviceContext;

    public HttpRedirectService() {
    }

    @Override public String getType() {
        return "http.redirect";
    }

    @Override public void init(ServiceContext serviceContext) throws Exception {
        this.serviceContext = serviceContext;
        handler = new HttpRedirectServiceHandler(logger);
        ServiceProperties properties = serviceContext.getProperties();

    }

    @Override public void start() throws Exception {
        serviceContext.bind(serviceContext.getAccepts(), handler);
    }

    @Override public void stop() throws Exception {
        quiesce();

        if (serviceContext != null) {
            for (IoSession session : serviceContext.getActiveSessions()) {
                session.close(true);
            }
        }
    }

    @Override public void quiesce() throws Exception {
        if (serviceContext != null) {
            serviceContext.unbind(serviceContext.getAccepts(), handler);
        }
    }

    @Override public void destroy() throws Exception {
    }
}
