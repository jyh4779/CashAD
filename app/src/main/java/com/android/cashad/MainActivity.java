package com.android.cashad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    private Button onBtn, offBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        onBtn= (Button)findViewById(R.id.button);
        offBtn= (Button)findViewById(R.id.button2);
        onBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScreenService.class);
                startService(intent);
            }

        });
        offBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScreenService.class);
                stopService(intent);
            }
        });
    }
    public void checkPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)) {
                Uri uri = Uri.fromParts("package", "com.android.cashad", null);
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                startActivityForResult(intent, 0);
            } else {
                Intent intent = new Intent(getApplicationContext(), ScreenService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                }
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if(!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "해라", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(getApplicationContext(), ScreenService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                }
            }
        }
    }
}