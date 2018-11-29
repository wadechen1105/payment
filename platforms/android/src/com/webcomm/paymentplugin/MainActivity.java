/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.webcomm.paymentplugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.webcomm.cordova.plugin.payment.PaymentPlugin;
import com.webcomm.cordova.plugin.payment.PaymentsUtil;

import org.apache.cordova.CordovaActivity;

import static com.webcomm.cordova.plugin.payment.PaymentsUtil.LOAD_PAYMENT_DATA_REQUEST_CODE;

public class MainActivity extends CordovaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int result = PaymentPlugin.CANCEL;
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        PaymentsUtil.handlePaymentSuccess(paymentData);
                        result = PaymentPlugin.SUCCESS;
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        PaymentsUtil.handleError(status.getStatusCode());
                        result = PaymentPlugin.FAILED;
                        break;
                }

                break;
        }

        runJS(result);
    }

    void runJS(final int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String url = "javascript:eventsHandler.exec(\'" + status + "\')";
                MainActivity.this.appView.loadUrl(url);
            }
        });

    }

}
