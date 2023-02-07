package com.android.cashad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;


public class MainActivity extends AppCompatActivity {
    private Button onBtn, offBtn, adBtn;
    private TextView cashText;
    private RewardedAd mRewardedAd;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        PointInit();

        onBtn = (Button)findViewById(R.id.button);
        offBtn = (Button)findViewById(R.id.button2);
        adBtn = (Button)findViewById(R.id.adbutton);
        onBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScreenService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startService(intent);
            }

        });
        offBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScreenService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stopService(intent);
            }
        });
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                loadAdsRequest();
            }
        });
        adBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Show Ads", Toast.LENGTH_SHORT).show();
                if (mRewardedAd != null) {
                    mRewardedAd.show(MainActivity.this, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Handle the reward.
                            Log.d(TAG, "The user earned the reward.");
                            int rewardAmount = rewardItem.getAmount();
                            String rewardType = rewardItem.getType();

                            SharedPreferences settings = getSharedPreferences("CashAD",MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            int adReward = settings.getInt("CashPoint", -5) + 3;

                            editor.putInt("CashPoint", adReward);
                            editor.commit();
                        }
                    });
                } else {
                    Log.d(TAG, "The rewarded ad wasn't ready yet.");
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences("CashAD",MODE_PRIVATE);

        cashText = (TextView) findViewById(R.id.cashViewer);
        cashText.setText(String.valueOf(settings.getInt("CashPoint",-1)));
    }
    public void checkPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)) {
                Uri uri = Uri.fromParts("package", "com.android.cashad", null);
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                startActivityForResult(intent, 0);
            } else {
                Intent intent = new Intent(getApplicationContext(), ScreenService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

    public void PointInit() {
        int dPoint = -1;
        SharedPreferences settings = getSharedPreferences("CashAD",MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if (dPoint == settings.getInt("CashPoint",-1)){
            editor.putInt("CashPoint", 0);
            editor.commit();
        }
        else{
            Log.d(String.valueOf(this),"dPoint = "+dPoint);
        }
    }
    private void loadAdsRequest() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                //Handle the error.
                Log.d(TAG, loadAdError.getMessage());
                mRewardedAd = null;
            }
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd){
                mRewardedAd = rewardedAd;
                Log.d(TAG, "Ad was loaded");
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent(){
                        // Called when ad is shown.
                        Log.d(TAG, "Ad was shown");
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError){
                        // Called when ad fails to show.
                        Log.d(TAG, "Ad failed to show.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        //Called when ad is dismissed.
                        //Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        Log.d(TAG, "Ad was dismissed.");
                        loadAdsRequest();
                    }
                });
            }
        });
    }
    /*
    void createNotificationChannel(String channelId, String channelName, int importance)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, importance));
        }
    }

    void createNotification(String channelId, int id, String title, String text, Intent intent)
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0,
                intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)    // 클릭시 설정된 PendingIntent가 실행된다
                .setAutoCancel(true)                // true이면 클릭시 알림이 삭제된다
                //.setTimeoutAfter(1000)
                //.setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }

    void destroyNotification(int id)
    {
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    private void loadAds(){
        AdRequest adRequest = new AdRequest.Builder().build();

        //RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/5224354917",
                adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAnLoaded(InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent(){

                    }
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }
                });
            }
                });

    }*/
}