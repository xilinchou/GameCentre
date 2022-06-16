package com.gamecentre.classicgames.tank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.activity.MenuActivity;
import com.gamecentre.classicgames.connection.ClientConnectionThread;
import com.gamecentre.classicgames.connection.ServerConnectionThread;
import com.gamecentre.classicgames.model.Game;
import com.gamecentre.classicgames.model.TankGameModel;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;
import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.utils.RemoteMessageListener;
import com.gamecentre.classicgames.utils.WifiDialogListener;
import com.gamecentre.classicgames.wifidirect.WifiDialog;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TankMenuActivity extends AppCompatActivity implements WifiDialogListener, ServiceListener, RemoteMessageListener {

    TankTextView grenadeTxt, helmetTxt, clockTxt, shovelTxt, tankTxt,starTxt, gunTxt, boatTxt, goldTxt, retryTxt, retryTmr;
    ImageView shopImg;
    boolean firstTime = true;

    TankTextView inviteTxt;
    private AdView mAdView;
    private InterstitialAd interstitialAd;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    private RewardedAd mRewardedAd;
    private static final String RAD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    boolean isLoading;
    public static boolean GOT_REWARD = false;

    private boolean opened = false;

    SharedPreferences settings;

    static Intent intent = null;

    public static final String TWO_PLAYERS = "two players";
    public static final String
            PREF_MUTED = "muted",
            PREF_VIBRATE = "vibrate",
            PREF_LEVEL = "level";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tank_menu);
        MessageRegister.getInstance().setMsgListener(this);
        settings = getSharedPreferences("TankSettings", 0);

        grenadeTxt = findViewById(R.id.grenadeCountTxt);
        helmetTxt = findViewById(R.id.shieldCountTxt);
        clockTxt = findViewById(R.id.clockCountTxt);
        shovelTxt = findViewById(R.id.shovelCountTxt);
        tankTxt = findViewById(R.id.tankCountTxt);
        starTxt = findViewById(R.id.starCountTxt);
        gunTxt = findViewById(R.id.gunCountTxt);
        boatTxt = findViewById(R.id.boatCountTxt);
        goldTxt = findViewById(R.id.goldCountTxt);
        shopImg = findViewById(R.id.shop);
        retryTxt = findViewById(R.id.retryTxt);
        retryTmr = findViewById(R.id.menuRetryTmrTxt);

        updateBonus();

        if(intent == null) {
            intent = getIntent();
        }
        Bundle b = intent.getExtras();
        String tankType = b.getString(TankTypeActivity.TANK_TYPE, "");

        ((TankTextView)findViewById(R.id.tanktype)).setText(tankType);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loadAd();
        loadRewardedAd();



        shopImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    openStore();
                }
                return false;
            }
        });


        MessageRegister.getInstance().setwifiDialogListener(this);
        setListeners();
        inviteTxt = (TankTextView) findViewById(R.id.ivName);
        inviteTxt.setSelected(true);

        int newDay = checkNewDay();
        Log.d("DATE CHECK", String.valueOf(newDay));
        if(newDay > 0 && firstTime){
            openReward(newDay);
        }

        MessageRegister.getInstance().setServiceListener(this);
        opened = true;
    }

    protected void onResume() {
        super.onResume();
        opened = true;
    }

    @Override
    protected void onDestroy() {
        opened = false;
        super.onDestroy();
    }


    public void onServiceMessageReceived(int games, long time_left) {
        if(opened){
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            Log.d("SERVICE MESSAGE MENU", String.valueOf(games) + " " + time_left);
            retryTxt.setText(String.valueOf(games));
            if(games >= CONST.Tank.MAX_GAME_COUNT) {
                retryTmr.setText("");
            }
            else {
                retryTmr.setText(sdf.format(time_left));
            }
        }
    }

    @Override
    public void onMessageReceived(Game message) {
        if(message instanceof TankGameModel) {
            TankGameModel msg = (TankGameModel)message;

            if(WifiDirectManager.getInstance().isServer() && ServerConnectionThread.serverStarted)
            {
                if (msg.playerInfo) {
                    TankStageDialog.p2Ready = msg.playerReady;
                    Log.d("Msg From P2", "Ready: " +" "+ TankStageDialog.p2Ready);
                }
            }

        }
    }

    public int checkNewDay() {
//        Date date = null;
//        String str = "Jul 30 2003 23:11:52.454 UTC";
//        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz", Locale.ENGLISH);
//        try{
//            date = df.parse(str);
//        }
//        catch (ParseException e) {
//            Log.d("DATE CHECK", "PARSE EXCEPTION");
//            return 0;
//        }
//        long epoch = date.getTime();


        long newDay;
        int numDays = 0;
        long lastDay = settings.getLong(TankActivity.LAST_DAY,0);
        long currentDay = (long) (System.currentTimeMillis() / 86400000);
//        long currentDay = (long) (epoch / 86400000);

        newDay = currentDay - lastDay;

        numDays = settings.getInt(TankActivity.CONSECUTIVE_DAYS,0);

        if(newDay == 1) {
            numDays++;
            if(numDays > 7) {
                numDays = 1;
            }
        }
        else if(newDay > 1) {
            numDays = 1;
        }
        else{
            firstTime = false;
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(TankActivity.LAST_DAY,currentDay);
        editor.putInt(TankActivity.CONSECUTIVE_DAYS,numDays);
        editor.apply();

        return numDays;
    }

    public void openReward(int day) {
        TankDailyRewardDialog wd = new TankDailyRewardDialog(TankMenuActivity.this, day);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        wd.show();
        wd.getWindow().setAttributes(lp);
    }


    public void openStore() {
        TankPurchaseDialog wd = new TankPurchaseDialog(TankMenuActivity.this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        wd.show();
        wd.getWindow().setAttributes(lp);
    }


    public void updateBonus() {
        grenadeTxt.setText(String.valueOf(settings.getInt(TankActivity.GRENADE,3)));
        helmetTxt.setText(String.valueOf(settings.getInt(TankActivity.SHIELD,3)));
        clockTxt.setText(String.valueOf(settings.getInt(TankActivity.CLOCK,3)));
        shovelTxt.setText(String.valueOf(settings.getInt(TankActivity.SHOVEL,3)));
        tankTxt.setText(String.valueOf(settings.getInt(TankActivity.TANK,3)));
        starTxt.setText(String.valueOf(settings.getInt(TankActivity.STAR,3)));
        gunTxt.setText(String.valueOf(settings.getInt(TankActivity.GUN,3)));
        boatTxt.setText(String.valueOf(settings.getInt(TankActivity.BOAT,3)));
        goldTxt.setText(String.valueOf(settings.getInt(TankActivity.GOLD,3)));
        retryTxt.setText(String.valueOf(settings.getInt(TankActivity.RETRY_COUNT,5)));
    }

    protected void setListeners () {
        this.findViewById(R.id.tnkP1menu)
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent m) {
                        if(m.getAction() == MotionEvent.ACTION_DOWN) {
                            openStages(v, false);
                        }
                        return true;
                    }
                });

        this.findViewById(R.id.tnkP2menu)
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent m) {
                        if(m.getAction() == MotionEvent.ACTION_DOWN) {
                            if ((WifiDirectManager.getInstance().isServer() && ServerConnectionThread.serverStarted) ||
                                    (!WifiDirectManager.getInstance().isServer() && ClientConnectionThread.serverStarted)) {
                                openStages(v, true);
                            } else {
                                Toast toast = Toast.makeText(TankMenuActivity.this.getApplicationContext(),
                                        "Invite a player first",
                                        Toast.LENGTH_SHORT);

                                ViewGroup group = (ViewGroup) toast.getView();
                                TextView messageTextView = (TextView) group.getChildAt(0);
                                messageTextView.setTextSize(20);

                                toast.show();
                            }
                        }
                        return true;
                    }
                });

        this.findViewById(R.id.tnkConstmenu)
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent m) {
                        if(m.getAction() == MotionEvent.ACTION_DOWN) {
                            Log.d("Construction", "Opening fragment");
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.fragmentFrame,new ConstructionFragment());
                            fragmentTransaction.addToBackStack("cFragment");
                            fragmentTransaction.commit();
                        }
                        return true;
                    }
                });

        this.findViewById(R.id.tnkExitmenu)
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent m) {
                        if(m.getAction() == MotionEvent.ACTION_DOWN) {
                            Intent i = new Intent(TankMenuActivity.this, TankTypeActivity.class);
                            TankMenuActivity.this.startActivity(i);
                            TankMenuActivity.this.finish();
                        }
                        return true;
                    }
                });

//        this.findViewById(R.id.settingsBtn)
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        openSettings(view);
//                    }
//                });

        this.findViewById(R.id.inviteBtn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        showInterstitial();

//                        openPlayerSearchView();
                    }
                });
    }

    public void startGame(boolean twoPlayers) {
        Intent i = new Intent(this, TankActivity.class);
        i.putExtra(TankMenuActivity.TWO_PLAYERS, twoPlayers);
        startActivity(i);
        finish();
    }

//    public void openSettings(View view) {
//        TankSettingsDialog cdd = new TankSettingsDialog(this);
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(cdd.getWindow().getAttributes());
//        cdd.show();
//        cdd.getWindow().setAttributes(lp);
//    }


    public void openStages(View view, boolean twoPlayers) {
        TankStageDialog cdd = new TankStageDialog(this, twoPlayers);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(cdd.getWindow().getAttributes());
        cdd.show();
        cdd.getWindow().setAttributes(lp);
    }

    public void openPlayerSearchView() {
        WifiDirectManager.getInstance().initialize(TankMenuActivity.this);
        WifiDirectManager.getInstance().registerBReceiver();

        WifiDialog wd = new WifiDialog(TankMenuActivity.this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(wd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        wd.show();
        wd.getWindow().setAttributes(lp);
    }

    @Override
    public void onWifiDilogClosed() {
//        this.getView().update(true);
        if(WifiDirectManager.getInstance().isServer() && ServerConnectionThread.serverStarted) {
            findViewById(R.id.inviteBtn).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.p1,null));
            inviteTxt = (TankTextView) findViewById(R.id.ivName);
            inviteTxt.setText(WifiDirectManager.getInstance().getDeviceName());
            inviteTxt.setSelected(true);

        }
        else if(!WifiDirectManager.getInstance().isServer() && ClientConnectionThread.serverStarted) {
            findViewById(R.id.inviteBtn).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.p2,null));
            inviteTxt = (TankTextView) findViewById(R.id.ivName);
            inviteTxt.setText(WifiDirectManager.getInstance().getDeviceName());
            inviteTxt.setSelected(true);
        }
        else {
            inviteTxt.setText(R.string.invite_def_txt);
        }
    }

    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                AD_UNIT_ID,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        TankMenuActivity.this.interstitialAd = interstitialAd;
                        Log.i("Interstitial Ad", "onAdLoaded");
                        Toast.makeText(TankMenuActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        TankMenuActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                        openPlayerSearchView();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        TankMenuActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                        openPlayerSearchView();
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("Interstitial Ad", loadAdError.getMessage());
                        interstitialAd = null;

                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        Toast.makeText(
                                TankMenuActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(this);
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            openPlayerSearchView();
        }
    }


    public void loadRewardedAd() {
        if (mRewardedAd == null) {
            isLoading = true;
            GOT_REWARD = false;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    RAD_UNIT_ID,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d("Rewarded Ads", loadAdError.getMessage());
                            mRewardedAd = null;
                            TankMenuActivity.this.isLoading = false;
                            Toast.makeText(TankMenuActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            TankMenuActivity.this.mRewardedAd = rewardedAd;
                            Log.d("Rewarded Ads", "onAdLoaded");
                            TankMenuActivity.this.isLoading = false;
                            Toast.makeText(TankMenuActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    public void showRewardedVideo(Dialog purchaseDialog) {

        if (mRewardedAd == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }

        mRewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("Rewarded Ads", "onAdShowedFullScreenContent");
                        Toast.makeText(TankMenuActivity.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                        GOT_REWARD = false;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d("Rewarded Ads", "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedAd = null;
                        GOT_REWARD = false;
                        Toast.makeText(
                                TankMenuActivity.this, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedAd = null;
                        Log.d("Rewarded Ads", "onAdDismissedFullScreenContent");
                        Toast.makeText(TankMenuActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                        // Preload the next rewarded ad.
                        if(GOT_REWARD) {
                            SharedPreferences.Editor editor = settings.edit();
                            if(purchaseDialog instanceof TankPurchaseDialog) {
                                int goldCount = settings.getInt(TankActivity.GOLD, 0);
                                ((TankPurchaseDialog) purchaseDialog).goldCountTxt.setText(String.format("x%s", goldCount + 1));
                                editor.putInt(TankActivity.GOLD, goldCount + 1);
                                editor.apply();
                                SoundManager.playSound(Sounds.TANK.EARN_GOLD);
                            }
                            else if(purchaseDialog instanceof TankPurchaseGameDialog) {
                                int retryCount = settings.getInt(TankActivity.RETRY_COUNT, 0);
                                int amnt = Integer.parseInt(((TankPurchaseGameDialog) purchaseDialog).getContext().getResources().getString(R.string.adGameAmnt).replace("+", ""));
//                                ((TankPurchaseGameDialog) purchaseDialog).gameCountTxt.setText(String.format("x%s", retryCount + amnt));
                                editor.putInt(TankActivity.RETRY_COUNT, retryCount + 1);
                                editor.apply();
                                SoundManager.playSound(Sounds.TANK.EARN_GOLD);
                            }

                            if(purchaseDialog instanceof TankDailyRewardDialog) {
//                                int goldCount = settings.getInt(TankActivity.GOLD, 0);
//                                ((TankPurchaseDialog) purchaseDialog).goldCountTxt.setText(String.format("x%s", goldCount + 1));
//                                editor.putInt(TankActivity.GOLD, goldCount + 1);
//                                editor.apply();
//                                SoundManager.playSound(Sounds.TANK.EARN_GOLD);
                                ((TankDailyRewardDialog) purchaseDialog).setDoubleReward();
                            }
                        }
                        TankMenuActivity.this.loadRewardedAd();
                    }
                });
        Activity activityContext = TankMenuActivity.this;
        mRewardedAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("Rewarded Ads", "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        GOT_REWARD = true;

                    }
                });
    }
}