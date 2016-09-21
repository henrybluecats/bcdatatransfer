package com.bluecats.app.bcdatatransfer;

import com.bluecats.sdk.BlueCatsSDK;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by henrycheng on 19/09/2016.
 */
public class MyActivityBase extends AppCompatActivity {
    @Override
    protected void onPause() {
        BlueCatsSDK.didEnterBackground();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BlueCatsSDK.didEnterForeground();
    }
}
