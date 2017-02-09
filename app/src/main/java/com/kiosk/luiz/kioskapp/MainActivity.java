//
//  MainActivity.java
//
//  Created by Sandro Albert on 2016/07/11.
//  Copyright (c) Sandro Albert. All rights reserved.
//



package com.kiosk.luiz.kioskapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private WebView webView;
    private Handler handler = new Handler();
    private ImageView imgSetting;
    private RelativeLayout btnEye;
    private Button btnDone;
    private Button btnSave;
    private Button btnCancel;
    private Button btnUrlCancel;
    private Button btnAppExit;

    private Dialog dlgPassword;
    private Dialog dlgUrl;

    private EditText txtPass;
    private EditText txtUrl;

    private TextView lblErrorPas;

    private Boolean eyeFlag = false;



    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));


    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fullScreenAction();
        Log.v("MainActivity", "Oncreate()");

        // manage module for wifi
        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);

            final Runnable runnable = new Runnable() {
                public void run() {

                    WifiManager wifiManager = (WifiManager) MainActivity.this.getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager.isWifiEnabled()) {

                        handler.removeCallbacks(this);
                        init();
                    } else {
                        handler.postDelayed(this, 5000);
                    }
                }
            };
            runnable.run();
        } else {
            init();
        }
    }


    private void init(){



        // Retrieve a PendingIntent that will perform a broadcast
        setContentView(R.layout.activity_main);

        imgSetting = (ImageView) findViewById(R.id.img_setting);
        imgSetting.setOnClickListener(imgSettingClickListener);

        int chargePlug = getIntent().getIntExtra("chargerPlug", 0);
        Log.v("MainActivity", "chargePlug = " + chargePlug);

        webView = (WebView) findViewById(R.id.webview);
        hideOnScreenKeyboard();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String loadUrl = sharedPreferences.getString("url","");

        if (loadUrl.equals("")){
            loadUrl = "https://workstation.mywindowdressings.com/Station1/";
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            sharedPreferences.edit().putString("url",loadUrl).apply();
        }

        webView.setWebViewClient(new MyBrowser());
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        webView.loadUrl(loadUrl);
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {

                if (!isMyLauncherDefault()){
                    resetPreferredLauncherAndOpenChooser(MainActivity.this);
                }
            }
        });

        // relate password dialog
        dlgPassword = new Dialog(MainActivity.this);
        dlgPassword.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dlgPassword.setContentView(R.layout.dialog_password);

        btnEye = (RelativeLayout) dlgPassword.findViewById(R.id.btn_eye);
        findViewById(R.id.btn_eye);
        btnEye.setOnClickListener(btnEyeListener);

        txtPass = (EditText) dlgPassword.findViewById(R.id.txt_pass);
        txtPass.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        lblErrorPas = (TextView) dlgPassword.findViewById(R.id.lbl_pass);

        btnDone = (Button) dlgPassword.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(btnDoneClickListener);

        // relate url dialog
        dlgUrl = new Dialog(MainActivity.this);
        dlgUrl.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dlgUrl.setContentView(R.layout.dialog_url);

        txtUrl = (EditText) dlgUrl.findViewById(R.id.txt_url);
        txtUrl.setText(loadUrl);

        btnSave = (Button) dlgUrl.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(btnSaveClickListener);

        dlgPassword.setOnDismissListener(dialogOnDismissListener);

        btnCancel = (Button) dlgPassword.findViewById(R.id.btn_cancel);
        btnUrlCancel = (Button) dlgUrl.findViewById(R.id.btn_url_cancel);

        btnCancel.setOnClickListener(passwordCancelClickListener);
        btnUrlCancel.setOnClickListener(urlCancelClickListener);

        btnAppExit = (Button) dlgUrl.findViewById(R.id.btn_app_exit);
        btnAppExit.setOnClickListener(btnAPpExitClickListener);

    }

    // block back button
    @Override
    public void onBackPressed() {
        // nothing to do here
        // … really
    }

    @Override
    protected void onPause() {
        super.onPause();

        // block recent apps button
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);

    }


    // block Volume button
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    // block long power button
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
        fullScreenAction();
    }


    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private View.OnClickListener imgSettingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            dlgPassword.show();

            fullScreenAction();
        }
    };

    private View.OnClickListener btnAPpExitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getPackageManager().clearPackagePreferredActivities(getPackageName());
            finish();

        }
    };

    private boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, null);


        if(activities.size() == 0) //no default
            return true;

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, FakeLauncherActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }

    private final View.OnClickListener btnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

//            if (txtPass.getText().toString().equals("17091")){
//                lblErrorPas.setVisibility(View.INVISIBLE);
//                dlgPassword.dismiss();
//                dlgUrl.show();
//            } else {
//                lblErrorPas.setVisibility(View.VISIBLE);
//            }

            dlgPassword.dismiss();
            dlgUrl.show();

            fullScreenAction();

        }
    };

    private final View.OnClickListener btnSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            sharedPreferences.edit().putString("url",txtUrl.getText().toString()).apply();
            dlgUrl.dismiss();
            init();
            fullScreenAction();
        }
    };

    private final View.OnClickListener btnEyeListener = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(!eyeFlag) {
                txtPass.setInputType(InputType.TYPE_CLASS_NUMBER);;
            } else {
                txtPass.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            }

            eyeFlag =! eyeFlag;
        }
    });

    private final Dialog.OnDismissListener dialogOnDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
           fullScreenAction();
        }
    };

    private View.OnClickListener passwordCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            txtPass.setText("");
            dlgPassword.dismiss();
            fullScreenAction();
        }
    };

    private View.OnClickListener urlCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            txtPass.setText("");
            dlgUrl.dismiss();
            fullScreenAction();
        }
    };

    @Override
    public void onResume(){
        super.onResume();

       fullScreenAction();
    }

    private void fullScreenAction(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    private void hideOnScreenKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow( webView.getWindowToken(), 0);
    }

}
