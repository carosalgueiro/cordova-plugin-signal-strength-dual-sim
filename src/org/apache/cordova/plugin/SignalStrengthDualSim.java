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

        LOG.i(LOG_TAG, "STARTING");
        LOG.i(LOG_TAG, "Params: " + action);

        if (SIM_ONE_ASU.equals(action) || SIM_TWO_ASU.equals(action)) {

            Context context = cordova.getActivity().getApplicationContext();
            ssListener = new SignalStrengthStateListener();
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            defaultTelephonyManager.listen(ssListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


//            int counter = 0;
//            while (dbm == -1) {
//                LOG.i(LOG_TAG, "while " + dbm);
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//
//                    return false;
//                }
//                if (counter++ >= 5) {
//                    break; // return -1
//                }
//            }

//            callback.success();

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
            return true;
        }

        return false;
    }

    class SignalStrengthStateListener extends PhoneStateListener  {

//        CallbackContext callback;
//        Context context;
//        public void SignalStrengthStateListener(CallbackContext callbackContext, Context appContext){
//            callback = callbackContext;
//            context = appContext;
//        }

        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {

            super.onSignalStrengthsChanged(signalStrength);
            int tsNormSignalStrength = signalStrength.getGsmSignalStrength();
            LOG.i(LOG_TAG, "Signalstrength, " + tsNormSignalStrength);
            asu = tsNormSignalStrength;
            level = signalStrength.getLevel();


            Context context = cordova.getActivity().getApplicationContext();
            TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operatorName = defaultTelephonyManager.getNetworkOperatorName();
            String operator = defaultTelephonyManager.getNetworkOperator();
            int networkType = defaultTelephonyManager.getNetworkType();
    //        int networkDataType = defaultTelephonyManager.getDataNetworkType();

            String netWorkTypeName;
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    netWorkTypeName = "GPRS";
                    break;
                case TelephonyManager.NETWORK_TYPE_GSM:
                    netWorkTypeName = "GSM";
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    netWorkTypeName = "EDGE";
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    netWorkTypeName = "CDMA";
                    break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    netWorkTypeName = "1xRTT";
                    break;
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    netWorkTypeName = "IDEN";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    netWorkTypeName = "UMTS";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    netWorkTypeName = "EVDO_0";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    netWorkTypeName = "EVDO_A";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    netWorkTypeName = "HSDPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    netWorkTypeName = "HSUPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    netWorkTypeName = "HSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    netWorkTypeName = "EVDO_B";
                    break;
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    netWorkTypeName = "EHRPD";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    netWorkTypeName = "HSPAP";
                    break;
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    netWorkTypeName = "3G";
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    netWorkTypeName = "LTE";
                    break;
                case TelephonyManager.NETWORK_TYPE_IWLAN:
                    netWorkTypeName = "4G";
                    break;
                default:
                    netWorkTypeName = "unknown";
                    break;
            }

            
            
            
            List<CellSignalStrength> ListStuff = signalStrength.getCellSignalStrengths();
            
            String stuffDBM = "";
            String stuffAsuLevel = "";
            String stuffLevel = "";
            
   /*         ListStuff.forEach(function(entry) {
                stuffDBM = stuffDBM + "/" + entry.getDbm();
                stuffAsuLevel = stuffAsuLevel + "/" + getAsuLevel();
                stuffLevel = stuffLevel + "/" + getLevel();
            });
            
  */          
            
            
            JSONObject response = new JSONObject();

            try {
                response.put("operator_name", operatorName);
                response.put("operator", operator);
                response.put("networkType", netWorkTypeName);
                response.put("NetworkTypeI", networkType);
                response.put("asu", asu);
                response.put("level", level);
                response.put("stuffDBM", stuffDBM);
                response.put("stuffAsuLevel", stuffAsuLevel);
                response.put("stuffLevel", stuffLevel);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            PluginResult result = new PluginResult(PluginResult.Status.OK, response);
            result.setKeepCallback(false);
            callback.sendPluginResult(result);
        }
    }

}
