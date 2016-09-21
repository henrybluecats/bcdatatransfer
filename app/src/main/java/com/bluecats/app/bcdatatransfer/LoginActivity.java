package com.bluecats.app.bcdatatransfer;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bluecats.sdk.BCAccountManager;
import com.bluecats.sdk.BCAccountManagerCallback;
import com.bluecats.sdk.BCApp;
import com.bluecats.sdk.BCError;
import com.bluecats.sdk.BCPerson;
import com.bluecats.sdk.BlueCatsSDK;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.security.Permission;
import java.util.ArrayList;

public class LoginActivity extends MyActivityBase {

    private static final String TAG = "LoginActivity";
    public static final int PERMISSION_REQUEST = 0x110;
    private Button btn_login;
    private EditText et_apptoken;

    private MaterialDialog mDialog;
    private boolean isBackKeyAllowed = true;
    private Handler mHandler;
    private String mAppToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_apptoken = (EditText)findViewById(R.id.et_apptoken);
        btn_login = (Button)findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int len = et_apptoken.getText().length();
                if (et_apptoken.getText().length() != 36) {
                    invalidApptoken(null);
                    return;
                }
                mDialog.show();
                mAppToken = et_apptoken.getText().toString().trim();
                saveAppToken();
                startBlueCatsSDK();
                checkBlueCatsSDKStatus();
            }
        });
        isBackKeyAllowed = true;
        mDialog = new MaterialDialog.Builder(this)
                .title("Verifying App Token")
                .content("Waiting...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0)
                .build();
        mHandler = new Handler();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mAppToken = sp.getString(Utils.EXTRA_APP_TOKEN, null);
        if (checkPermission()) {
            readyToGo();
        }
    }

    private void readyToGo() {
        if (!TextUtils.isEmpty(mAppToken)) {
            goToMainActivity();
        }
    }
    private boolean checkPermission() {
        ArrayList<String> permissions = new ArrayList<>();
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(permission);
        }
        permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(permission);
        }
        permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(permission);
        }
        if (permissions.size() > 0) {
            String[] array = new String[permissions.size()];
            ActivityCompat.requestPermissions(this, permissions.toArray(array), PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            boolean allGranted = true;
            for (int i =0; i< grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                readyToGo();
            } else {
                Snackbar.make(et_apptoken, "Permission Error!!", Snackbar.LENGTH_INDEFINITE).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(sdkChecker);
        super.onDestroy();
    }

    private void checkBlueCatsSDKStatus() {
        mHandler.postDelayed(sdkChecker, 1000);
    }

    private Runnable sdkChecker = new Runnable() {
        @Override
        public void run() {
            if (BlueCatsSDK.getStatus() == BlueCatsSDK.BCStatus.BC_STATUS_PURRING) {
                goToMainActivity();
            } else if (BlueCatsSDK.getStatus() == BlueCatsSDK.BCStatus.BC_STATUS_PURRING_WITH_ERRORS) {
                Log.d(TAG, "purring with errors");
                goToMainActivity();
            } else if (BlueCatsSDK.getStatus() == BlueCatsSDK.BCStatus.BC_STATUS_STOPPED_PURRING) {
                mAppToken = null;
                invalidApptoken(BlueCatsSDK.getAppTokenVerificationStatus().name());
            } else {
                mHandler.postDelayed(sdkChecker, 1000);
            }
        }
    };

    private void goToMainActivity() {
        mDialog.dismiss();
        saveAppToken();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        startBlueCatsSDK();
        finish();
    }

    private void saveAppToken() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Utils.EXTRA_APP_TOKEN, mAppToken);
        editor.commit();
    }

    private void invalidApptoken(String err) {
        mDialog.dismiss();
        String text = err;
        if (TextUtils.isEmpty(text)) {
            text = "invalid app token";
        }
        Snackbar.make(et_apptoken, text, Snackbar.LENGTH_LONG).show();

    }

    private void startBlueCatsSDK() {
        Intent intent = new Intent(this, MySDKService.class);
        startService(intent);
    }

}
