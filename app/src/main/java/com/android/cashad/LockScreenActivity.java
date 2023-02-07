package com.android.cashad;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;

public class LockScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            //setTurnScreenOn(true)
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        // Show the ad.
                        TemplateView template = findViewById(R.id.my_template);
                        template.setNativeAd(nativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(50); // 시크바 최대값 설정
        seekBar.setProgress(0); // 초기 시크바 값 설정

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //TODO [SeekBar 컨트롤 진행 중]
                if(seekBar.getProgress() < 49){
                    seekBar.setProgress(0);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //TODO [SeekBar 터치 이벤트 발생]
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress > 48) {
                    SharedPreferences settings = getSharedPreferences("CashAD",MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();

                    int dPoint = settings.getInt("CashPoint",-2);
                    dPoint += 1;

                    Log.d(String.valueOf(this),"dPoint = "+dPoint);

                    editor.putInt("CashPoint", dPoint);
                    editor.commit();
                    finish();
                }
            }
        });
    }
}
