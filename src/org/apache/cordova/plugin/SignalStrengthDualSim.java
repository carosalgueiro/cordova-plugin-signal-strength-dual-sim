package org.apache.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;

import android.content.Context;

import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class SignalStrengthDualSim extends CordovaPlugin {

    int dbm = -1;
    int asu = 0;
    TelephonyManager mTelephonyManager;
    SignalStrengthStateListener ssListener;

    private static final String LOG_TAG = "CordovaPluginSignalStrengthDualSim";
    private static final String SIM_ONE_ASU = "Sim1";
    private static final String SIM_TWO_ASU = "Sim2"; // not supported at the moment.
    private static final String SIM_COUNT = "SimCount";
    private static final String HAS_READ_PERMISSION = "hasReadPermission";
    private static final String REQUEST_READ_PERMISSION = "requestReadPermission";
    private CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;

        LOG.i(LOG_TAG, "STARTING");
        LOG.i(LOG_TAG, "Params: " + action);

        if (SIM_COUNT.equals(action)) {

            List<SubscriptionInfo> subscriptions = mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptions == null) {
                this.callback.error("Subscriptions returned null");
                return false;
            }

            final int num = subscriptions.size();
            this.callback.success(num);

        } else if (SIM_ONE_ASU.equals(action) || SIM_TWO_ASU.equals(action)) {

            ssListener = new SignalStrengthStateListener();
            Context context = cordova.getActivity().getApplicationContext();
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            defaultTelephonyManager.listen(ssListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            int counter = 0;
            while (dbm == -1) {
                LOG.i(LOG_TAG, "while " + dbm);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {

                    return false;
                }
                if (counter++ >= 5) {
                    break; // return -1
                }
            }

            String operatorName = defaultTelephonyManager.getNetworkOperatorName();
            String operator = defaultTelephonyManager.getNetworkOperator();
            int networkType = defaultTelephonyManager.getNetworkType();

            String netWorkTypeName;
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    netWorkTypeName = "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    netWorkTypeName = "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    netWorkTypeName = "4G";
                default:
                    netWorkTypeName = "unknown";
                    break;
            }

            JSONObject response = new JSONObject();
            response.put('dbm', dbm);
            response.put('asu', asu);
            response.put('operator_name', operatorName);
            response.put('operator', operator);
            response.put('networkType', netWorkTypeName)

            callbackContext.success(response);
            return true;
        } else if (HAS_READ_PERMISSION.equals(action)) {
            hasReadPermission();
            return true;
        } else if (REQUEST_READ_PERMISSION.equals(action)) {
            requestReadPermission();
            return true;
        }

        return false;
    }


    private void hasReadPermission() {
        this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                simPermissionGranted(Manifest.permission.READ_PHONE_STATE)));
    }

    private void requestReadPermission() {
        requestPermission(Manifest.permission.READ_PHONE_STATE);
    }

    private boolean simPermissionGranted(String type) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        return cordova.hasPermission(type);
    }

    private void requestPermission(String type) {
        LOG.i(LOG_TAG, "requestPermission");
        if (!simPermissionGranted(type)) {
            cordova.requestPermission(this, 12345, type);
        } else {
            this.callback.success();
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.callback.success();
        } else {
            this.callback.error("Permission denied");
        }
    }

    class SignalStrengthStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            int tsNormSignalStrength = signalStrength.getGsmSignalStrength();
            LOG.i(LOG_TAG, "Signalstrength, " + tsNormSignalStrength);
            asu = tsNormSignalStrength;
            dbm = (2 * tsNormSignalStrength) - 113;
        }
    }

}