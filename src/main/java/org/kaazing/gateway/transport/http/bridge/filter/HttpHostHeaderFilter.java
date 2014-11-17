/**
 * Copyright (c) 2007-2014 Kaazing Corporation. All rights reserved.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.kaazing.gateway.transport.http.bridge.filter;

import java.util.List;
import java.util.Map;

import org.kaazing.gateway.transport.http.HttpStatus;
import org.kaazing.gateway.transport.http.bridge.HttpRequestMessage;
import org.kaazing.mina.core.session.IoSessionEx;

public class HttpHostHeaderFilter extends HttpFilterAdapter<IoSessionEx> {

    public static final String HEADER_HOST = "Host";

    @Override
    protected void httpRequestReceived(NextFilter nextFilter, IoSessionEx session, HttpRequestMessage httpRequest) throws Exception {
        Map<String, List<String>> headers = httpRequest.getHeaders();
        List<String> hostHeaderValues = headers.get(HEADER_HOST);
        // KG-12034 / KG-11219: Send 400 Bad Request when Host header is absent OR multiple Host header found OR value is empty
        // http://tools.ietf.org/html/rfc7230#section-5.4
        // A server MUST respond with a 400 (Bad Request) status code to any
        // HTTP/1.1 request message that lacks a Host header field and to any
        // request message that contains more than one Host header field or a
        // Host header field with an invalid field-value.
        if(hostHeaderValues == null || hostHeaderValues.size() != 1 || hostHeaderValues.get(0).isEmpty()) {
            throw new HttpProtocolDecoderException(HttpStatus.CLIENT_BAD_REQUEST);  
        }
        
        super.httpRequestReceived(nextFilter, session, httpRequest);
    }

}
