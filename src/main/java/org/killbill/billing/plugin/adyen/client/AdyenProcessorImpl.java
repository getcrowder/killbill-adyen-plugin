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
package org.killbill.billing.plugin.adyen.client;

import com.adyen.model.checkout.CreateCheckoutSessionResponse;
import com.adyen.model.checkout.PaymentRefundResponse;
import com.adyen.model.checkout.PaymentResponse;
import com.adyen.model.checkout.PaymentReversalResponse;
import com.adyen.service.exception.ApiException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.joda.time.LocalDate;
import org.killbill.billing.plugin.adyen.api.ProcessorInputDTO;
import org.killbill.billing.plugin.adyen.api.ProcessorOutputDTO;
import org.killbill.billing.plugin.adyen.api.SessionInputDTO;
import org.killbill.billing.plugin.adyen.api.SessionOutputDTO;
import org.killbill.billing.plugin.adyen.core.AdyenConfigurationHandler;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdyenProcessorImpl implements GatewayProcessor {
  private static final Logger logger = LoggerFactory.getLogger(AdyenProcessorImpl.class);

  private final AdyenSDKClientImpl httpClient;

  private static final String MERCHANT_ACCOUNT = "merchantAccount";
  private static final String API_KEY = "apiKey";

  public AdyenProcessorImpl(AdyenSDKClientImpl httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public ProcessorOutputDTO processOneTimePayment(ProcessorInputDTO input) {
    PaymentResponse response = null;
    try {
      response =
          httpClient.purchase(
              input.getCurrency(),
              input.getAmount(),
              input.getKbTransactionId(),
              input.getKbAccountId(),
              input.getRecurringData());
    } catch (IOException e) {
      logger.error("[Adyen] IO Exception{}", e.getMessage(), e);
      e.printStackTrace();
    } catch (ApiException e) {

      logger.error("[Adyen] API Exception {} \n {}", e.getError(), e.getMessage(), e);
      e.printStackTrace();
    }
    ProcessorOutputDTO outputDTO = new ProcessorOutputDTO();
    if (response != null) {
      outputDTO.setFirstPaymentReferenceId(response.getPspReference());
      outputDTO.setAdditionalData(response.getAdditionalData());
    }

    return outputDTO;
  }

  @Override
  public ProcessorOutputDTO processPayment(ProcessorInputDTO input) {
    CreateCheckoutSessionResponse response = null;
    boolean recurring = input.getPaymentMethod().toString().equals("RECURRING");
    try {
      response =
          httpClient.checkoutsessions(
              input.getCurrency(),
              input.getAmount(),
              input.getKbTransactionId(),
              input.getKbAccountId(),
              recurring);
    } catch (IOException e) {
      logger.error("[Adyen] IO Exception{}", e.getMessage(), e);
      e.printStackTrace();
    } catch (ApiException e) {

      logger.error("[Adyen] API Exception {} \n {}", e.getError(), e.getMessage(), e);
      e.printStackTrace();
    }

    ProcessorOutputDTO outputDTO = new ProcessorOutputDTO();
    if (response != null) {
      outputDTO.setFirstPaymentReferenceId(response.getId());
      outputDTO.setSecondPaymentReferenceId(response.getMerchantOrderReference());
      Map<String, String> additionalData = new HashMap<>();
      additionalData.put("sessionData", response.getSessionData());
      outputDTO.setAdditionalData(additionalData);
    }

    return outputDTO;
  }

  @Override
  public ProcessorOutputDTO refundPayment(ProcessorInputDTO input) {
    PaymentRefundResponse response = null;
    try {
      response =
          httpClient.refund(
              input.getCurrency(),
              input.getAmount(),
              input.getKbTransactionId(),
              input.getPspReference());
    } catch (IOException e) {
      logger.error("[Adyen] IO Exception{}", e.getMessage(), e);
      e.printStackTrace();
    } catch (ApiException e) {

      logger.error("[Adyen] API Exception {} \n {}", e.getError(), e.getMessage(), e);
      e.printStackTrace();
    }

    ProcessorOutputDTO outputDTO = new ProcessorOutputDTO();
    if (response != null) {
      outputDTO.setFirstPaymentReferenceId(response.getPspReference());
    }

    return outputDTO;
  }

  @Override
  public ProcessorInputDTO validateData(
      AdyenConfigurationHandler AdyenConfigurationHandler,
      Map<String, String> properties,
      UUID context,
      UUID kbAccountId) {

    ProcessorInputDTO inputDTO = new ProcessorInputDTO();

    inputDTO.setPluginProperties(properties);
    // Read Configuration From Kill Bill
    Map<String, String> configurations = new HashMap<>();
    if (AdyenConfigurationHandler.getConfigurable(context).getApiKey() == null
        || AdyenConfigurationHandler.getConfigurable(context).getMerchantAccount() == null) {
      return null;
    }
    configurations.put(
        MERCHANT_ACCOUNT, AdyenConfigurationHandler.getConfigurable(context).getMerchantAccount());
    configurations.put(API_KEY, AdyenConfigurationHandler.getConfigurable(context).getApiKey());
    inputDTO.setPluginConfiguration(configurations);
    TenantContext tenantContext = new PluginTenantContext(kbAccountId, context);
    String localdate = LocalDate.now().toString("yyyyMMdd");
    inputDTO.setTenantContext(tenantContext);
    inputDTO.setCreatedDate(localdate);
    inputDTO.setKbAccountId(kbAccountId.toString().replace("-", ""));
    return inputDTO;
  }

  @Override
  public ProcessorOutputDTO voidPayment(ProcessorInputDTO input) {
    PaymentReversalResponse response = null;
    try {
      response = httpClient.reversal(input.getKbTransactionId(), input.getPspReference());
    } catch (IOException e) {
      logger.error("[Adyen] IO Exception{}", e.getMessage(), e);
      e.printStackTrace();
    } catch (ApiException e) {

      logger.error("[Adyen] API Exception {} \n {}", e.getError(), e.getMessage(), e);
      e.printStackTrace();
    }

    ProcessorOutputDTO outputDTO = new ProcessorOutputDTO();
    if (response != null) {
      outputDTO.setFirstPaymentReferenceId(response.getPspReference());
    }

    return outputDTO;
  }

  @Override
  public SessionOutputDTO getSessionResult(final SessionInputDTO sessionInputDTO) {
    if (sessionInputDTO == null || sessionInputDTO.getSessionId() == null || sessionInputDTO.getSessionResult() == null) {
      logger.error("[Adyen] Invalid session data provided: sessionInput is null or missing required fields");
      return null;
    }
  
    SessionOutputDTO response = null;
    try {
      response = httpClient.getResultOfPaymentSession(sessionInputDTO);
      if (response == null) {
        logger.warn("[Adyen] No response received from Adyen for session ID: {}", sessionInputDTO.getSessionId());
      }
    } catch (IOException e) {
      logger.error("[Adyen] IO Exception processing session result: {}", e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("[Adyen] API Exception processing session result: {} - {}", e.getError(), e.getMessage(), e);
    } catch (Exception e) {
      logger.error("[Adyen] Unexpected error processing session result: {}", e.getMessage(), e);
    }
    return response;
  }
}
