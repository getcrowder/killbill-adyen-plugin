/*
 * Copyright 2020-2023 Equinix, Inc
 * Copyright 2014-2023 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.adyen.api;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for Adyen session input parameters.
 * Used to pass session information to retrieve results from Adyen.
 */
@Getter
@Setter
public class SessionInputDTO {

    /**
     * The ID of the Adyen payment session
     */
    private String sessionId;
    
    /**
     * The session result data provided by Adyen
     */
    private String sessionResult;
}
