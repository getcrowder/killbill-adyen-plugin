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
import com.adyen.model.checkout.SessionResultResponse;
import com.adyen.service.exception.ApiException;

import java.io.IOException;
import java.math.BigDecimal;

import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.plugin.adyen.api.SessionInputDTO;
import org.killbill.billing.plugin.adyen.api.SessionOutputDTO;

public interface AdyenSDKClient {

    public CreateCheckoutSessionResponse checkoutsessions(
            Currency currency,
            BigDecimal kbAmount,
            String transactionId,
            String kbAccountId,
            boolean isRecurring)
            throws IOException, ApiException;

    public PaymentReversalResponse reversal(String transactionId, String paymentPspReference)
            throws IOException, ApiException;

    public PaymentRefundResponse refund(
            Currency currency, BigDecimal kbAmount, String transactionId, String paymentPspReference)
            throws IOException, ApiException;

    public PaymentResponse purchase(
            Currency currency,
            BigDecimal kbAmount,
            String transactionId,
            String kbAccountId,
            String recurringDetailReference)
            throws IOException, ApiException;

    public SessionOutputDTO getResultOfPaymentSession(SessionInputDTO sessionInputDTO)
            throws IOException, ApiException;
}
