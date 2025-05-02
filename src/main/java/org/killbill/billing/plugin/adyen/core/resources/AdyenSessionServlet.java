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

import java.util.Map;
import java.util.UUID;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jooby.mvc.Local;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillClock;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.plugin.adyen.core.AdyenActivator;
import org.killbill.billing.plugin.api.PluginCallContext;
import org.killbill.billing.plugin.core.PluginServlet;
import org.killbill.billing.tenant.api.Tenant;
import org.killbill.billing.util.callcontext.CallContext;

import com.google.inject.Inject;

@Singleton
@Path("/session")
public class AdyenSessionServlet extends PluginServlet {

  private final OSGIKillbillClock clock;
  private final AdyenSessionService service;

  @Inject
  public AdyenSessionServlet(final OSGIKillbillClock clock, AdyenSessionService service) {
    this.clock = clock;
    this.service = service;
  }

  @GET
  public Map<String, String> checkSessionResult(
      @Named("kbAccountId") final UUID kbAccountId,
      @Named("sessionId") final String sessionId,
      @Named("sessionResult") final String sessionResult,
      @Local @Named("killbill_tenant") final Tenant tenant)
      throws PaymentPluginApiException {

    // Validar parámetros de entrada
    if (kbAccountId == null) {
      throw new PaymentPluginApiException("INVALID_PARAMETER", "Account ID cannot be null");
    }
    
    if (sessionId == null || sessionId.trim().isEmpty()) {
      throw new PaymentPluginApiException("INVALID_PARAMETER", "Session ID cannot be null or empty");
    }
    
    if (sessionResult == null || sessionResult.trim().isEmpty()) {
      throw new PaymentPluginApiException("INVALID_PARAMETER", "Session result cannot be null or empty");
    }
  
    final CallContext context =
        new PluginCallContext(
            AdyenActivator.PLUGIN_NAME, clock.getClock().getUTCNow(), kbAccountId, tenant.getId());

    return service.getSessionResult(kbAccountId, context, sessionId, sessionResult, tenant.getId());
  }

}
