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

import com.adyen.Client;
import com.adyen.enums.Environment;
import com.adyen.model.checkout.Amount;
import com.adyen.model.checkout.CheckoutPaymentMethod;
import com.adyen.model.checkout.CreateCheckoutSessionRequest;
import com.adyen.model.checkout.CreateCheckoutSessionRequest.RecurringProcessingModelEnum;
import com.adyen.model.checkout.CreateCheckoutSessionRequest.ShopperInteractionEnum;
import com.adyen.model.checkout.CreateCheckoutSessionRequest.StorePaymentMethodModeEnum;
import com.adyen.model.checkout.CreateCheckoutSessionResponse;

import com.adyen.model.checkout.PaymentRefundRequest;
import com.adyen.model.checkout.PaymentRefundResponse;
import com.adyen.model.checkout.PaymentRequest;
import com.adyen.model.checkout.PaymentResponse;
import com.adyen.model.checkout.PaymentReversalRequest;
import com.adyen.model.checkout.PaymentReversalResponse;
import com.adyen.model.checkout.SessionResultResponse;
import com.adyen.model.checkout.StoredPaymentMethodDetails;
import com.adyen.service.checkout.ModificationsApi;
import com.adyen.service.checkout.PaymentsApi;
import com.adyen.service.exception.ApiException;

import java.io.IOException;
import java.math.BigDecimal;

import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.plugin.adyen.api.SessionInputDTO;
import org.killbill.billing.plugin.adyen.api.SessionOutputDTO;
import org.killbill.billing.plugin.adyen.core.AdyenConfigProperties;

public class AdyenSDKClientImpl implements AdyenSDKClient {

    private final AdyenConfigProperties adyenConfigProperties;
    private final PaymentsApi paymentsApi;
    private final ModificationsApi modificationsApi;

    public AdyenSDKClientImpl(AdyenConfigProperties adyenConfigProperties) {
        this.adyenConfigProperties = adyenConfigProperties;
        final Client client =
                new Client(
                        adyenConfigProperties.getApiKey(),
                        Environment.valueOf(adyenConfigProperties.getEnviroment()));
        this.paymentsApi = new PaymentsApi(client);
        this.modificationsApi = new ModificationsApi(client);
    }

    @Override
    public CreateCheckoutSessionResponse checkoutsessions(
            Currency currency,
            BigDecimal kbAmount,
            String kbTransactionId,
            String kbAccountId,
            boolean isRecurrent)
            throws IOException, ApiException {

        Amount amount = new Amount().currency(currency.name()).value(convertToMinorUnit(kbAmount));
        CreateCheckoutSessionRequest checkoutSession = new CreateCheckoutSessionRequest();
        checkoutSession.merchantAccount(adyenConfigProperties.getMerchantAccount());
        checkoutSession.setChannel(CreateCheckoutSessionRequest.ChannelEnum.WEB);
        checkoutSession.setReference(kbTransactionId);
        checkoutSession.setReturnUrl(adyenConfigProperties.getReturnUrl());
        checkoutSession.setAmount(amount);
        checkoutSession.setCountryCode(adyenConfigProperties.getRegion());
        checkoutSession.setCaptureDelayHours(
                Integer.valueOf(adyenConfigProperties.getCaptureDelayHours()));
        checkoutSession.setShopperReference(kbAccountId);
        if (isRecurrent) {
            checkoutSession.setRecurringProcessingModel(RecurringProcessingModelEnum.CARDONFILE);
            checkoutSession.shopperInteraction(ShopperInteractionEnum.ECOMMERCE);
//          checkoutSession.setEnableOneClick(true);
//          checkoutSession.setEnableRecurring(true);
            checkoutSession.storePaymentMethod(true);
            checkoutSession.storePaymentMethodMode(StorePaymentMethodModeEnum.ENABLED);
        }

        return paymentsApi.sessions(checkoutSession);
    }

    @Override
    public PaymentReversalResponse reversal(String transactionId, String paymentPspReference)
            throws IOException, ApiException {

        PaymentReversalRequest paymentReversalRequest = new PaymentReversalRequest();

        paymentReversalRequest.setMerchantAccount(adyenConfigProperties.getMerchantAccount());
        paymentReversalRequest.setReference(transactionId);

        return modificationsApi.refundOrCancelPayment(paymentPspReference, paymentReversalRequest);
    }

    public PaymentRefundResponse refund(
            Currency currency, BigDecimal kbAmount, String transactionId, String paymentPspReference)
            throws IOException, ApiException {

        PaymentRefundRequest paymentRefundRequest = new PaymentRefundRequest();
        Amount amount = new Amount().currency(currency.name()).value(convertToMinorUnit(kbAmount));
        paymentRefundRequest.setAmount(amount);
        paymentRefundRequest.setMerchantAccount(adyenConfigProperties.getMerchantAccount());
        paymentRefundRequest.setReference(transactionId);
        return modificationsApi.refundCapturedPayment(paymentPspReference, paymentRefundRequest);
    }

    @Override
    public PaymentResponse purchase(
            Currency currency,
            BigDecimal kbAmount,
            String transactionId,
            String kbAccountId,
            String recurringDetailReference)
            throws IOException, ApiException {
        PaymentRequest paymentsRequest = new PaymentRequest();
        Amount amount = new Amount().currency(currency.name()).value(convertToMinorUnit(kbAmount));
        paymentsRequest.setAmount(amount);
        paymentsRequest.setReference(transactionId);

        paymentsRequest.setShopperReference(kbAccountId);
        paymentsRequest.setReturnUrl(adyenConfigProperties.getReturnUrl());
        paymentsRequest.setMerchantAccount(adyenConfigProperties.getMerchantAccount());
        paymentsRequest.setShopperInteraction(PaymentRequest.ShopperInteractionEnum.CONTAUTH);
        paymentsRequest.setRecurringProcessingModel(PaymentRequest.RecurringProcessingModelEnum.UNSCHEDULEDCARDONFILE);
        // antes
        // paymentsRequest.addOneClickData(recurringDetailReference, null);
        // despues
        StoredPaymentMethodDetails storedPaymentMethodDetails = new StoredPaymentMethodDetails();
        // storedPaymentMethodDetails.setType(TypeEnum.BCMC_MOBILE);
        storedPaymentMethodDetails.storedPaymentMethodId(recurringDetailReference);
        // storedPaymentMethodDetails.setRecurringDetailReference(recurringDetailReference);

        paymentsRequest.setPaymentMethod(new CheckoutPaymentMethod(storedPaymentMethodDetails));

        paymentsRequest.setCaptureDelayHours(
                Integer.valueOf(adyenConfigProperties.getCaptureDelayHours()));

        return paymentsApi.payments(paymentsRequest);
    }

    @Override
    public SessionOutputDTO getResultOfPaymentSession(final SessionInputDTO sessionInputDTO)
            throws IOException, ApiException {

        final SessionResultResponse resultOfPaymentSession = paymentsApi
                .getResultOfPaymentSession(sessionInputDTO.getSessionId(), sessionInputDTO.getSessionResult());

        final SessionOutputDTO sessionOutputDTO = new SessionOutputDTO();
        sessionOutputDTO.setSessionId(resultOfPaymentSession.getId());
        sessionOutputDTO.setSessionStatus(resultOfPaymentSession.getStatus().toString());
        return sessionOutputDTO;
    }

    private Long convertToMinorUnit(BigDecimal amount) {

        String minorUnit = amount.toString().replace(".", "");

        return Long.valueOf(minorUnit);
    }

}
