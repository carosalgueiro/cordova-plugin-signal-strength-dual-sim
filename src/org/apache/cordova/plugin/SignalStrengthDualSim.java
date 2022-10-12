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

    int dBM = 0;
	int asu = 0;
    int level = 0;
    SignalStrengthStateListener ssListener;
    TelephonyManager defaultTelephonyManager;

    private static final String LOG_TAG = "CordovaPluginSignalStrengthDualSim";
    private static final String SIM_ONE_ASU = "Sim1";
    private static final String SIM_TWO_ASU = "Sim2"; // not supported at the moment.
    private static final String SIM_COUNT = "SimCount";
    private static final String HAS_READ_PERMISSION = "hasReadPermission";
    private static final String REQUEST_READ_PERMISSION = "requestReadPermission";
    CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;

        if (SIM_ONE_ASU.equals(action) || SIM_TWO_ASU.equals(action)) {

            Context context = cordova.getActivity().getApplicationContext();
            ssListener = new SignalStrengthStateListener();
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            defaultTelephonyManager.listen(ssListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
            return true;
        }

        return false;
    }

    class SignalStrengthStateListener extends PhoneStateListener  {

        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {

            super.onSignalStrengthsChanged(signalStrength);


            Context context = cordova.getActivity().getApplicationContext();
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operatorName = defaultTelephonyManager.getNetworkOperatorName();
            String operator = defaultTelephonyManager.getNetworkOperator();

            
            
            List<android.telephony.CellSignalStrength> ListStuff = signalStrength.getCellSignalStrengths();
            
            
            for (android.telephony.CellSignalStrength entry: ListStuff) {
                dBM = entry.getDbm();
                asu = entry.getAsuLevel();
                level = entry.getLevel();
            }
            
            
            JSONObject response = new JSONObject();

            try {
                response.put("operator_name", operatorName);
                response.put("operator", operator);
                response.put("asu", asu);
                response.put("level", level);
                response.put("dBM", dBM);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            PluginResult result = new PluginResult(PluginResult.Status.OK, response);
            result.setKeepCallback(false);
            callback.sendPluginResult(result);
        }
    }

}
