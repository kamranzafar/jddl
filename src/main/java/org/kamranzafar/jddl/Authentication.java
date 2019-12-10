/**
 * Copyright 2012 Kamran Zafar
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kamranzafar.jddl;

import org.kamranzafar.jddl.util.Base64;

public class Authentication {

    public enum AuthType {
        BASIC, BEARER
    }

    private AuthType authType = AuthType.BASIC;

    private String token;

    private Authentication() {
    }

    public static Authentication withBasicAuthentication(final String username, final String password) {
        Authentication auth = new Authentication();
        auth.authType = AuthType.BASIC;
        auth.token = String.format("Basic %s", Base64.encodeBytes(String.format("%s:%s", username, password).getBytes()));

        return auth;
    }

    public static Authentication withBearerAuthentication(final String token) {
        Authentication auth = new Authentication();
        auth.authType = AuthType.BEARER;
        auth.token = String.format("Bearer %s", token);

        return auth;
    }

    public String getRequestHeader() {
        return this.token;
    }

}
