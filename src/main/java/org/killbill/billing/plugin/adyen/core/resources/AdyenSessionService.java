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

package org.killbill.billing.plugin.adyen.core.resources;

import static org.killbill.billing.plugin.adyen.api.AdyenPaymentPluginApi.INTERNAL;
import static org.killbill.billing.plugin.adyen.api.AdyenPaymentPluginApi.SESSION_DATA;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.plugin.adyen.api.SessionInputDTO;
import org.killbill.billing.plugin.adyen.api.SessionOutputDTO;
import org.killbill.billing.plugin.adyen.client.GatewayProcessor;
import org.killbill.billing.plugin.adyen.client.GatewayProcessorFactory;
import org.killbill.billing.plugin.adyen.core.AdyenConfigurationHandler;
import org.killbill.billing.util.callcontext.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class to handle Adyen session operations
 */
public class AdyenSessionService {

    private static final Logger logger = LoggerFactory.getLogger(AdyenSessionService.class);

    private OSGIKillbillAPI killbillAPI;
    private AdyenConfigurationHandler adyenConfigurationHandler;

    public AdyenSessionService(
            OSGIKillbillAPI killbillAPI, AdyenConfigurationHandler adyenConfigurationHandler) {
        this.killbillAPI = killbillAPI;
        this.adyenConfigurationHandler = adyenConfigurationHandler;
    }

    /**
     * Retrieves and validates the session result from Adyen.
     * This method authenticates with Kill Bill, retrieves the session status from Adyen,
     * validates that the session is in a completed state, and returns the session details.
     *
     * @param kbAccountId   The Kill Bill account ID associated with the payment
     * @param context       The Kill Bill call context for this operation
     * @param sessionId     The Adyen session ID to retrieve
     * @param sessionResult The session result string provided by Adyen
     * @param tenantId      The tenant ID for multi-tenancy support
     * @return A map containing the session details including status and session ID
     * @throws PaymentPluginApiException if authentication fails, if the session cannot be retrieved,
     *                                   or if the session is not in a "completed" state
     */
    public Map<String, String> getSessionResult(final UUID kbAccountId, final CallContext context, String sessionId, String sessionResult, UUID tenantId)
            throws PaymentPluginApiException {
        logger.info("[Adyen] Retrieving session result for sessionId: {}", sessionId);
        // TODO: Validar que kbAccountId pertenece al pago.
        
        try {
            // Authenticate with Kill Bill
            killbillAPI
                    .getSecurityApi()
                    .login(
                            adyenConfigurationHandler.getConfigurable(tenantId).getUsername(),
                            adyenConfigurationHandler.getConfigurable(tenantId).getPassword());
    
            final SessionInputDTO sessionInputDTO = new SessionInputDTO();
            sessionInputDTO.setSessionId(sessionId);
            sessionInputDTO.setSessionResult(sessionResult);
    
            GatewayProcessor gatewayProcessor =
                    GatewayProcessorFactory.get(
                            adyenConfigurationHandler.getConfigurable(context.getTenantId()));
    
            final SessionOutputDTO sessionResultDTO = gatewayProcessor.getSessionResult(sessionInputDTO);
            
            if (sessionResultDTO == null) {
                throw new PaymentPluginApiException(INTERNAL, "Failed to retrieve session result from Adyen");
            }
    
            // Validate session status
            if (!"completed".equals(sessionResultDTO.getSessionStatus())) {
                throw new PaymentPluginApiException(
                        "Adyen Payment for session: " + sessionId + ", Status: " + sessionResultDTO.getSessionStatus(),
                        "Payment session not completed");
            }
    
            final Map<String, String> formFields = new HashMap<>();
            formFields.put("sessionId", sessionResultDTO.getSessionId());
            formFields.put(SESSION_DATA, sessionResultDTO.getSessionStatus());
            return formFields;
        
        } catch (Exception e) {
            logger.error("[Adyen] Error retrieving session result", e);
            throw new PaymentPluginApiException(INTERNAL, "Error retrieving session result: " + e.getMessage());
        } finally {
            // Logout from Kill Bill
            killbillAPI.getSecurityApi().logout();
        }
    }

}
