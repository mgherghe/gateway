/*
 * Copyright 2014, Kaazing Corporation. All rights reserved.
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

package org.kaazing.gateway.transport.wsn.specification.httpx;

import static org.kaazing.test.util.ITUtil.createRuleChain;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.kaazing.gateway.server.test.GatewayRule;
import org.kaazing.gateway.server.test.config.GatewayConfiguration;
import org.kaazing.gateway.server.test.config.builder.GatewayConfigurationBuilder;
import org.kaazing.k3po.junit.annotation.Specification;
import org.kaazing.k3po.junit.rules.K3poRule;

public class ExtendedHandshakeIT {

    private final K3poRule k3po = new K3poRule().setScriptRoot("org/kaazing/specification/httpx/extended");

    private GatewayRule gateway = new GatewayRule() {
        {
            GatewayConfiguration configuration =
                    new GatewayConfigurationBuilder()
                        .service()
                            .accept(URI.create("ws://localhost:8000/echo"))
                            .type("echo")
                            .crossOrigin()
                                .allowOrigin("*")
                            .done()
                        .done()
                        .service()
                            .accept(URI.create("ws://localhost:8000/echoauth"))
                            .type("echo")
                            .realmName("Kaazing Gateway Demo")
                            .crossOrigin()
                                .allowOrigin("*")
                            .done()
                            .authorization()
                                .requireRole("AUTHORIZED")
                            .done()
                        .done()
                        .security()
                            .realm()
                                .name("Kaazing Gateway Demo")
                                .description("Kaazing Gateway Demo")
                                .httpChallengeScheme("Application Basic")
                                .authorizationMode("challenge")
                                .loginModule()
                                    .type("class:org.kaazing.gateway.transport.wsn.auth.TestBasicLoginModule")
                                    .success("requisite")
                                    .option("roles", "AUTHORIZED")
                                .done()
                            .done()
                        .done()
                    .done();
            init(configuration);
        }
    };

    @Rule
    public TestRule chain = createRuleChain(gateway, k3po);

    @Test
    @Specification({"connection.established.with.authorization/request"})
    public void shouldEstablishConnectionWithAuthorization() throws Exception {
        k3po.finish();
    }

    @Test
    @Specification({"connection.established/request"})
    public void shouldEstablishConnection() throws Exception {
        k3po.finish();
    }

    @Test
    @Specification({"client.sends.message.between.opening.and.extended.handshake/request"})
    public void shouldFailWhenClientSendsMessageBetweenOpeningAndExtendedHandshake() throws Exception {
        k3po.finish();
    }

    @Test
    @Ignore ("Not ready yet")
    @Specification({"server.sends.message.between.opening.and.extended.handshake/request"})
    public void shouldFailWhenServerSendsMessageBetweenOpeningAndExtendedHandshake() throws Exception {
        k3po.finish();
    }

    @Test
    @Ignore ("Not ready yet")
    @Specification({"extension.in.opening.and.extended.handshake/request"})
    public void shouldFailWhenExtendedHandShakeHasExtensionFromOpeningHandshake() throws Exception {
        k3po.finish();
    }

    @Test
    @Ignore ("Not ready yet")
    @Specification({"extension.in.opening.handshake/request"})
    public void shouldPassWhenExtensionIsNegotiatedInOpeningHandshake() throws Exception {
        k3po.finish();
    }

    @Test
    @Ignore ("Not ready yet")
    @Specification({"extension.in.extended.handshake/request"})
    public void shouldPassWhenExtensionIsNegotiatedInExtendedHandshake() throws Exception {
        k3po.finish();
    }

    @Test
    @Ignore ("Not ready yet")
    @Specification({"extended.handshake.response.code.200/request"})
    public void shouldFailWhenWebSocketProtocolGets200StatusCode() throws Exception {
        k3po.finish();
    }

    @Test
    @Ignore ("Not ready yet")
    @Specification({"extended.handshake.response.code.101/request"})
    public void shouldPassWhenWebSocketProtocolGets101StatusCode() throws Exception {
        k3po.finish();
    }

    @Test
    @Ignore ("Not ready yet")
    @Specification({"extended.handshake.response.code.302/request"})
    public void shouldPassWhenWebSocketProtocolGets302StatusCode() throws Exception {
        k3po.finish();
    }

    @Test
    @Ignore ("Not ready yet")
    @Specification({"extended.handshake.response.code.401/request"})
    public void shouldPassWhenWebSocketProtocolGets401StatusCode() throws Exception {
        k3po.finish();
    }
}
