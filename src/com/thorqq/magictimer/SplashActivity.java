package com.thorqq.magictimer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGHT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        PackageManager pm = getPackageManager();
        TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
        try {
               PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
               versionNumber.setText("Version " + pi.versionName);
        } 
        catch (NameNotFoundException e) 
        {
            versionNumber.setText("Unknown Version");
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,
                        MagicTimerActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }

        }, SPLASH_DISPLAY_LENGHT);

    }
}