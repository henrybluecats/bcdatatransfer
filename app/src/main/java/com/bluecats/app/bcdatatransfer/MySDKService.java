package com.bluecats.app.bcdatatransfer;

import com.bluecats.sdk.BCLogManager;
import com.bluecats.sdk.BlueCatsSDK;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MySDKService extends Service {
    private static final String TAG = "MySDKService";

    public MySDKService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String appToken = sp.getString(Utils.EXTRA_APP_TOKEN, null);
        if (TextUtils.isEmpty(appToken)) {
            Log.e(TAG, "app token is empyt, service stopped.");
            BlueCatsSDK.stopPurring();
            return super.onStartCommand(intent, flags, startId);
        }

        Map<String, String> options = new HashMap<>();
        options.put(BlueCatsSDK.BC_OPTION_CROWD_SOURCE_BEACON_UPDATES, "false");
        BlueCatsSDK.setOptions(options);
        BCLogManager.getInstance().setLogLevel(BCLogManager.BC_LOG_TYPE_SCANNER, BCLogManager.BC_LOG_LEVEL_MORE);
        BlueCatsSDK.startPurringWithAppToken(getApplicationContext(), appToken);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
