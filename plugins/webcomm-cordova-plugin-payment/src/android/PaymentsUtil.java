/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webcomm.cordova.plugin.payment;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.ShippingAddressRequirements;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

/**
 * Contains helper static methods for dealing with the Payments API.
 * <p>
 * Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
public class PaymentsUtil {
    public static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 999;
    private static PaymentsClient sClient;

    private PaymentsUtil() {
    }

    /**
     * Creates an instance of {@link PaymentsClient} for use in an {@link Activity} using the
     * environment and theme set in {@link Constants}.
     *
     * @param activity is the caller's activity.
     */
    public synchronized static PaymentsClient getPaymentsClient(Activity activity) {

        if (sClient == null) {
            Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                    .setEnvironment(Constants.PAYMENTS_ENVIRONMENT)
                    .build();
            sClient = Wallet.getPaymentsClient(activity, walletOptions);
        }
        return sClient;
    }

    public static void recycleClient() {
        sClient = null;
    }

    /**
     * Builds {@link PaymentDataRequest} to be consumed by {@link PaymentsClient#loadPaymentData}.
     *
     * @param transactionInfo contains the price for this transaction.
     */
    public static PaymentDataRequest createPaymentDataRequestByGateway(TransactionInfo transactionInfo) {
        PaymentMethodTokenizationParameters.Builder paramsBuilder =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                        .addParameter("gateway", Constants.GATEWAY_TOKENIZATION_NAME);
        for (Pair<String, String> param : Constants.GATEWAY_TOKENIZATION_PARAMETERS) {
            paramsBuilder.addParameter(param.first, param.second);
        }

        return createPaymentDataRequest(transactionInfo, paramsBuilder.build());
    }

    /**
     * Builds {@link PaymentDataRequest} for use with DIRECT integration to be consumed by
     * {@link PaymentsClient#loadPaymentData}.
     * <p>
     * Please refer to the documentation for more information about DIRECT integration. The type of
     * integration you use depends on your payment processor.
     *
     * @param transactionInfo contains the price for this transaction.
     */
    public static PaymentDataRequest createPaymentDataRequestDirect(TransactionInfo transactionInfo) {
        PaymentMethodTokenizationParameters params =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_DIRECT)

                        // Omitting the publicKey will result in a request for unencrypted data.
                        // Please refer to the documentation for more information on unencrypted
                        // requests.
                        .addParameter("publicKey", Constants.DIRECT_TOKENIZATION_PUBLIC_KEY)
                        .build();

        return createPaymentDataRequest(transactionInfo, params);
    }

    private static PaymentDataRequest createPaymentDataRequest(TransactionInfo transactionInfo, PaymentMethodTokenizationParameters params) {
        PaymentDataRequest request =
                PaymentDataRequest.newBuilder()
                        .setPhoneNumberRequired(false)
                        .setEmailRequired(true)
                        .setShippingAddressRequired(true)
                        // Omitting ShippingAddressRequirements all together means all countries are
                        // supported.
                        .setShippingAddressRequirements(
                                ShippingAddressRequirements.newBuilder()
                                        .addAllowedCountryCodes(Constants.SHIPPING_SUPPORTED_COUNTRIES)
                                        .build())

                        .setTransactionInfo(transactionInfo)
                        .addAllowedPaymentMethods(Constants.SUPPORTED_METHODS)
                        .setCardRequirements(
                                CardRequirements.newBuilder()
                                        .addAllowedCardNetworks(Constants.SUPPORTED_NETWORKS)
                                        .setAllowPrepaidCards(true)
                                        .setBillingAddressRequired(true)

                                        // Omitting this parameter will result in the API returning
                                        // only a "minimal" billing address (post code only).
                                        .setBillingAddressFormat(WalletConstants.BILLING_ADDRESS_FORMAT_FULL)
                                        .build())
                        .setPaymentMethodTokenizationParameters(params)

                        // If the UI is not required, a returning user will not be asked to select
                        // a card. Instead, the card they previously used will be returned
                        // automatically (if still available).
                        // Prior whitelisting is required to use this feature.
                        .setUiRequired(true)
                        .build();

        return request;
    }

    /**
     * Determines if the user is eligible to Pay with Google by calling
     * {@link PaymentsClient#isReadyToPay}. The nature of this check depends on the methods set in
     * {@link Constants#SUPPORTED_METHODS}.
     * <p>
     * If {@link WalletConstants#PAYMENT_METHOD_CARD} is specified among supported methods, this
     * function will return true even if the user has no cards stored. Please refer to the
     * documentation for more information on how the check is performed.
     *
     * @param client used to send the request.
     */
    public static Task<Boolean> isReadyToPay(PaymentsClient client) {
        IsReadyToPayRequest.Builder request = IsReadyToPayRequest.newBuilder();
        for (Integer allowedMethod : Constants.SUPPORTED_METHODS) {
            request.addAllowedPaymentMethod(allowedMethod);
        }
        return client.isReadyToPay(request.build());
    }

    /**
     * The price is not displayed to the user and must be in the following format: "12.34".
     *
     * @param price total of the transaction.
     */
    public static TransactionInfo createTransaction(String price) {
        return TransactionInfo.newBuilder()
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .setTotalPrice(price)
                .setCurrencyCode(Constants.CurrencyCode.TWD)
                .build();
    }


    public static void handlePaymentSuccess(PaymentData paymentData) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        //
        // Refer to your processor's documentation on how to proceed from here.
        PaymentMethodToken token = paymentData.getPaymentMethodToken();

        // getPaymentMethodToken will only return null if PaymentMethodTokenizationParameters was
        // not set in the PaymentRequest.
        if (token != null) {
            String billingName = paymentData.getCardInfo().getBillingAddress().getName();

            // Use token.getToken() to get the token string.
            Log.d("PaymentData", "billingName = " + billingName);
        }
    }

    public static void handleError(int statusCode) {
        // At this stage, the user has already seen a popup informing them an error occurred.
        // Normally, only logging is required.
        // statusCode will hold the value of any constant from CommonStatusCode or one of the
        // WalletConstants.ERROR_CODE_* constants.
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

}
