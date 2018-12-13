package com.webcomm.cordova.plugin.payment;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import static com.webcomm.cordova.plugin.payment.PaymentsUtil.LOAD_PAYMENT_DATA_REQUEST_CODE;

/**
 * This class echoes a string called from JavaScript.
 */
public class PaymentPlugin extends CordovaPlugin {
    public static final int SUCCESS = 1;
    public static final int FAILED = -1;
    public static final int CANCEL = 0;

    private Activity mActivity;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mActivity = cordova.getActivity();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("checkIsReadyToPay")) {
            checkIsReadyToPay(callbackContext);
            return true;
        } else if (action.equals("requestPayment")) {
            requestPayment(args.getString(0));
            return true;
        }
        return false;
    }

    private void checkIsReadyToPay(final CallbackContext callbackContext) {

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        PaymentsUtil.isReadyToPay(PaymentsUtil.getPaymentsClient(mActivity)).addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(Task<Boolean> task) {
                        try {
                            Boolean r = task.getResult(ApiException.class);
                            callbackContext.success(r.booleanValue() ? 1 : 0);
                        } catch (ApiException exception) {
                            // Process error
                            Log.w("isReadyToPay failed", exception);
                        }
                    }
                });
    }


    // This method is called when the Pay with Google button is clicked.
    // The price provided to the API should include taxes and shipping.
    // This price is not displayed to the user.
    private void requestPayment(String totalPrice) {
        TransactionInfo transaction = PaymentsUtil.createTransaction(totalPrice);

//******//        PaymentDataRequest request = PaymentsUtil.createPaymentDataRequest(transaction); // gateway

        PaymentDataRequest request = PaymentsUtil.createPaymentDataRequestDirect(transaction); // direct
        PaymentsClient client = PaymentsUtil.getPaymentsClient(mActivity);
        Task<PaymentData> futurePaymentData = client.loadPaymentData(request);

//         Since loadPaymentData may show the UI asking the user to select a payment method, we use
//         AutoResolveHelper to wait for the user interacting with it. Once completed,
//         onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(futurePaymentData, mActivity, LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

}