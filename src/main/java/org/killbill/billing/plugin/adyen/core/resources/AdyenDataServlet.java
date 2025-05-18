package org.killbill.billing.plugin.adyen.core.resources;

import com.google.inject.Inject;

import org.jooby.mvc.GET;
import org.jooby.mvc.Local;
import org.jooby.mvc.Path;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillClock;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.plugin.adyen.core.AdyenActivator;
import org.killbill.billing.plugin.api.PluginCallContext;
import org.killbill.billing.plugin.core.PluginServlet;
import org.killbill.billing.tenant.api.Tenant;
import org.killbill.billing.util.callcontext.CallContext;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
@Path("/data")
public class AdyenDataServlet extends PluginServlet {

    private final OSGIKillbillClock clock;
    private final AdyenDataService service;

    @Inject
    public AdyenDataServlet(final OSGIKillbillClock clock, final AdyenDataService service) {
        this.clock = clock;
        this.service = service;
    }

    @GET
    public List<Map<String, String>> getTransactionsBySessionId(
            @Named("kbAccountId") final UUID kbAccountId,
            @Named("sessionId") final String sessionId,
            @Local @Named("killbill_tenant") final Tenant tenant)
            throws PaymentPluginApiException {

        if (kbAccountId == null) {
            throw new PaymentPluginApiException("INVALID_PARAMETER", "Account ID cannot be null");
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new PaymentPluginApiException("INVALID_PARAMETER", "Session ID cannot be null or empty");
        }

        final CallContext context =
                new PluginCallContext(
                        AdyenActivator.PLUGIN_NAME, clock.getClock().getUTCNow(), kbAccountId, tenant.getId());

        return service.getTransactionsByCheckoutSessionId(kbAccountId, context, sessionId, tenant.getId());
    }
}
