package org.killbill.billing.plugin.adyen.core.resources;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.plugin.adyen.dao.AdyenDao;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenNotificationsRecord;
import org.killbill.billing.util.callcontext.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AdyenDataService {

    private static final Logger logger = LoggerFactory.getLogger(AdyenDataService.class);
    
    private final AdyenDao adyenDao;

    @Inject
    public AdyenDataService(final AdyenDao adyenDao) {
        this.adyenDao = adyenDao;
    }

    public List<Map<String, String>> getTransactionsByCheckoutSessionId(final UUID kbAccountId, final CallContext context, final String sessionId, final UUID tenantId)
            throws PaymentPluginApiException {
        try {
            final List<AdyenNotificationsRecord> records = adyenDao.getNotificationsByCheckoutSessionId(kbAccountId, sessionId, tenantId);
            return convertRecordsToMaps(records);
        } catch (SQLException e) {
            logger.error("Error retrieving transaction data for sessionId {} and account {}", sessionId, kbAccountId, e);
            throw new PaymentPluginApiException("Error retrieving transaction data", e);
        }
    }
    
    private List<Map<String, String>> convertRecordsToMaps(List<AdyenNotificationsRecord> records) {
        List<Map<String, String>> result = new ArrayList<>();
        
        for (AdyenNotificationsRecord record : records) {
            Map<String, String> responseMap = new HashMap<>();
            
            responseMap.put("kbPaymentId", record.getKbPaymentId());
            responseMap.put("kbPaymentTransactionId", record.getKbPaymentTransactionId());
            responseMap.put("transactionType", record.getTransactionType());
            responseMap.put("amount", record.getAmount());
            responseMap.put("currency", record.getCurrency());
            responseMap.put("transactionStatus", record.getTransactionStatus());
            responseMap.put("pspReference", record.getPspReference());
            responseMap.put("createdDate", record.getCreatedDate());
            responseMap.put("additionalData", AdyenDao.mapFromAdditionalDataString(record.getAdditionalData()));
            
            result.add(responseMap);
        }
        
        return result;
    }
}
