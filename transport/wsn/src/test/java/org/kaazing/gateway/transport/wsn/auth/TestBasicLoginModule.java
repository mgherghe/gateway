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

package org.kaazing.gateway.transport.wsn.auth;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * A simple basic authentication login module.
 */
public class TestBasicLoginModule implements LoginModule {

    // initial state
    private Subject subject;
    private Map<String, ?> sharedState;

    // the authentication status
    private boolean succeeded;
    private boolean commitSucceeded;

    // testUser's RolePrincipal
    private RolePrincipal userPrincipal;
    
    private List<String> roles = new ArrayList<>();

    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.sharedState = sharedState;

        resolveRoles(options);
    }

    private void resolveRoles(Map<String, ?> options) {
        String roleString = (String) options.get("roles");
        if (roleString != null && null != roles)  {
            String[] roleNames = roleString.split(",");
            for (int i = 0; i < roleNames.length; i++) {
                if (roleNames[i] != null) {
                    roleNames[i] = roleNames[i].trim();
                }
            }
            roles = Arrays.asList(roleNames);
        }
        else {
            roles = Arrays.asList("USER");
        }
    }

    @Override
    public boolean login() throws LoginException {
        // verify the username/password
        String username = (String)sharedState.get("javax.security.auth.login.name");
        char[] password = (char[])sharedState.get("javax.security.auth.login.password");
        if (username == null || password == null) {
            throw new FailedLoginException("No UserName/Password to authenticate");
        }
        if (username.equals("joe") && password.length == 7 && password[0] == 'w' &&
                password[1] == 'e' && password[2] == 'l' && password[3] == 'c' &&
                password[4] == 'o' && password[5] == 'm' && password[6] == 'e') {
            // authentication succeeded!!!
            succeeded = true;
            return true;
        } else {
            // authentication failed
            succeeded = false;
            throw new FailedLoginException("UserName/Password is Incorrect");
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (!succeeded) {
            return false;
        } else {
            if (roles != null) {
                for (String role : roles) {
                    subject.getPrincipals().add(new RolePrincipal(role));
                }
            }
            return true;
        }
    }

    @Override
    public boolean abort() throws LoginException {
        if (!succeeded) {
            return false;
        } else if (!commitSucceeded) {
            // login succeeded but overall authentication failed
            succeeded = false;
            userPrincipal = null;
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        succeeded = false;
        commitSucceeded = false;
        userPrincipal = null;
        return true;
    }

    private static class RolePrincipal implements Principal {

        private final String name;

        public RolePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return("Role:  " + name);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;

            if (this == o)
                return true;

            if (!(o instanceof RolePrincipal))
                return false;
            RolePrincipal that = (RolePrincipal)o;

            return this.getName().equals(that.getName());
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

}
