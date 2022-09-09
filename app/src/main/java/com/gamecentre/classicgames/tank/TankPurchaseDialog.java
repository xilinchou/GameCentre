package com.gamecentre.classicgames.tank;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;
import com.gamecentre.classicgames.utils.CONST;

public class TankPurchaseDialog extends Dialog implements View.OnClickListener{
    TankView mTankView;

    public AppCompatActivity activity;
    public Dialog dialog;
    public TankTextView watchBtn;
    public ImageView cancelBtn;
    SharedPreferences settings;
    int goldCount;
//    TankTextView goldCountTxt;

    TankTextView grenadeTxt, helmetTxt, clockTxt, shovelTxt, tankTxt,starTxt, gunTxt, boatTxt, goldTxt, retryTxt;


    private static String TOTAL_GOLD = "TOTAL GOLD";

    public TankPurchaseDialog(AppCompatActivity a, TankView mTankView) {
        super(a);
        this.activity = a;
        this.mTankView = mTankView;
    }

    public TankPurchaseDialog(AppCompatActivity a) {
        super(a);
        this.activity = a;
        this.mTankView = null;
        this.dialog = null;
    }

    public TankPurchaseDialog(AppCompatActivity a, Dialog d) {
        super(a);
        this.activity = a;
        this.mTankView = null;
        this.dialog = d;
    }




    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setContentView(R.layout.activity_tank_purchase);
        setCancelable(false);

        watchBtn = findViewById(R.id.watchBtn);
        cancelBtn = findViewById(R.id.buyCancelBtn);
        watchBtn.setOnClickListener(this);
        findViewById(R.id.watchgold).setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        findViewById(R.id.starboat1).setOnClickListener(this);
        findViewById(R.id.starboat2).setOnClickListener(this);
        findViewById(R.id.starboat3).setOnClickListener(this);
        findViewById(R.id.starboat4).setOnClickListener(this);


        findViewById(R.id.clockshovel1).setOnClickListener(this);
        findViewById(R.id.clockshovel2).setOnClickListener(this);
        findViewById(R.id.clockshovel3).setOnClickListener(this);
        findViewById(R.id.clockshovel4).setOnClickListener(this);


        findViewById(R.id.gunhelmet1).setOnClickListener(this);
        findViewById(R.id.gunhelmet2).setOnClickListener(this);
        findViewById(R.id.gunhelmet3).setOnClickListener(this);
        findViewById(R.id.gunhelmet4).setOnClickListener(this);


        findViewById(R.id.tankgrenade1).setOnClickListener(this);
        findViewById(R.id.tankgrenade2).setOnClickListener(this);
        findViewById(R.id.tankgrenade3).setOnClickListener(this);
        findViewById(R.id.tankgrenade4).setOnClickListener(this);

        findViewById(R.id.buy_game).setOnClickListener(this);
        findViewById(R.id.game2).setOnClickListener(this);
        findViewById(R.id.game3).setOnClickListener(this);
        findViewById(R.id.game4).setOnClickListener(this);

        findViewById(R.id.buy_game6).setOnClickListener(this);
        findViewById(R.id.game62).setOnClickListener(this);
        findViewById(R.id.game63).setOnClickListener(this);
        findViewById(R.id.game64).setOnClickListener(this);

        findViewById(R.id.buy_goldAd).setOnClickListener(this);
        findViewById(R.id.goldAd2).setOnClickListener(this);
        findViewById(R.id.goldAd3).setOnClickListener(this);
        findViewById(R.id.goldAd4).setOnClickListener(this);

        findViewById(R.id.buy_gold).setOnClickListener(this);
        findViewById(R.id.gold2).setOnClickListener(this);
        findViewById(R.id.gold3).setOnClickListener(this);

//        goldCountTxt = (TankTextView)findViewById(R.id.stashTxt);

        settings = activity.getSharedPreferences("TankSettings", 0);

//        goldCount = settings.getInt(TankActivity.GOLD, 0);
//        goldCountTxt.setText(String.format("x%s", goldCount));


        grenadeTxt = findViewById(R.id.grenadeCountTxt);
        helmetTxt = findViewById(R.id.shieldCountTxt);
        clockTxt = findViewById(R.id.clockCountTxt);
        shovelTxt = findViewById(R.id.shovelCountTxt);
        tankTxt = findViewById(R.id.tankCountTxt);
        starTxt = findViewById(R.id.starCountTxt);
        gunTxt = findViewById(R.id.gunCountTxt);
        boatTxt = findViewById(R.id.boatCountTxt);
        goldTxt = findViewById(R.id.goldCountTxt);
        retryTxt = findViewById(R.id.retryTxt);

        updateBonus();
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


    @Override
    public void onClick(View v) {
        int cost;
        int goldCount = settings.getInt(TankActivity.GOLD,0);
        int id = v.getId();
        if (id == R.id.starboat1 || id == R.id.starboat2 || id == R.id.starboat3 || id == R.id.starboat4) {
            cost = Integer.parseInt((activity.getResources().getString(R.string.starboat_gold).replace("x", "")));
            if (cost <= goldCount) {
                int star = settings.getInt(TankActivity.STAR, 0);
                int boat = settings.getInt(TankActivity.BOAT, 0);

                int amount = Integer.parseInt((activity.getResources().getString(R.string.starboat_count).replace("x", "")));
                star += amount;
                boat += amount;
                goldCount -= cost;
                goldTxt.setText(String.format("%s", goldCount));
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(TankActivity.STAR, star);
                editor.putInt(TankActivity.STAR, boat);
                editor.putInt(TankActivity.GOLD, goldCount);
                editor.apply();
                SoundManager.playSound(Sounds.TANK.BUY_ITEM);
            } else {
                // TODO Not enough gold
                Log.d("PURCHASE STARBOAT", "Not enough gold");
            }
        }
        else if (id == R.id.clockshovel1 || id == R.id.clockshovel2 || id == R.id.clockshovel3 || id == R.id.clockshovel4) {
            cost = Integer.parseInt((activity.getResources().getString(R.string.clockshovel_gold).replace("x", "")));
            if (cost <= goldCount) {
                int clock = settings.getInt(TankActivity.CLOCK, 0);
                int shovel = settings.getInt(TankActivity.SHOVEL, 0);

                int amount = Integer.parseInt((activity.getResources().getString(R.string.clockshovel_count).replace("x", "")));
                clock += amount;
                shovel += amount;
                goldCount -= cost;
                goldTxt.setText(String.format("%s", goldCount));
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(TankActivity.CLOCK, clock);
                editor.putInt(TankActivity.SHOVEL, shovel);
                editor.putInt(TankActivity.GOLD, goldCount);
                editor.apply();
                SoundManager.playSound(Sounds.TANK.BUY_ITEM);
            } else {
                // TODO Not enough gold
                Log.d("PURCHASE CLOCKSHOVEL", "Not enough gold");
            }
        }
        else if (id == R.id.gunhelmet1 || id == R.id.gunhelmet2 || id == R.id.gunhelmet3 || id == R.id.gunhelmet4) {
            cost = Integer.parseInt((activity.getResources().getString(R.string.gunhelmet_gold).replace("x", "")));
            if (cost <= goldCount) {
                int gun = settings.getInt(TankActivity.GUN, 0);
                int helmet = settings.getInt(TankActivity.SHIELD, 0);

                int amount = Integer.parseInt((activity.getResources().getString(R.string.gunhelmet_count).replace("x", "")));
                gun += amount;
                helmet += amount;
                goldCount -= cost;
                goldTxt.setText(String.format("%s", goldCount));
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(TankActivity.GUN, gun);
                editor.putInt(TankActivity.SHIELD, helmet);
                editor.putInt(TankActivity.GOLD, goldCount);
                editor.apply();
                SoundManager.playSound(Sounds.TANK.BUY_ITEM);
            } else {
                // TODO Not enough gold
                Log.d("PURCHASE GUNHELMET", "Not enough gold");
            }
        }
        else if (id == R.id.tankgrenade1 || id == R.id.tankgrenade2 || id == R.id.tankgrenade3 || id == R.id.tankgrenade4) {
            cost = Integer.parseInt((activity.getResources().getString(R.string.tankgrenade_gold).replace("x", "")));
            if (cost <= goldCount) {
                int tank = settings.getInt(TankActivity.TANK, 0);
                int grenade = settings.getInt(TankActivity.GRENADE, 0);

                int amount = Integer.parseInt((activity.getResources().getString(R.string.tankgrenade_count).replace("x", "")));
                tank += amount;
                grenade += amount;
                goldCount -= cost;
                goldTxt.setText(String.format("%s", goldCount));
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(TankActivity.TANK, tank);
                editor.putInt(TankActivity.GRENADE, grenade);
                editor.putInt(TankActivity.GOLD, goldCount);
                editor.apply();
                SoundManager.playSound(Sounds.TANK.BUY_ITEM);
            } else {
                // TODO Not enough gold
                Log.d("PURCHASE TANKGRENADE", "Not enough gold");
            }
        }
        else if (id == R.id.buy_game || id == R.id.game2 || id == R.id.game3 || id == R.id.game4){
            cost = Integer.parseInt((activity.getResources().getString(R.string.game_gold).replace("x", "")));
            if (cost <= goldCount) {
                int game_count = settings.getInt(TankActivity.RETRY_COUNT, 0);
                int amount = Integer.parseInt((activity.getResources().getString(R.string.game_count).replace("x", "")));
                 game_count += amount;

                goldCount -= cost;
                goldTxt.setText(String.format("%s", goldCount));
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(TankActivity.RETRY_COUNT, game_count);
                editor.putInt(TankActivity.GOLD, goldCount);
                editor.apply();
                SoundManager.playSound(Sounds.TANK.BUY_ITEM);
            } else {
                // TODO Not enough gold
                Log.d("PURCHASE TANKGRENADE", "Not enough gold");
            }

        }
        else if (id == R.id.buy_game6 || id == R.id.game62 || id == R.id.game63 || id == R.id.game64){
            cost = Integer.parseInt(activity.getResources().getString(R.string.game6h_gold).replace("x", ""));
            if (cost <= goldCount) {
                goldCount -= cost;
                goldTxt.setText(String.format("%s", goldCount));
                long time_6h = System.currentTimeMillis() + CONST.Tank.LIFE_DURATION_6HRS;
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(TankActivity.GOLD, goldCount);
                editor.putLong(TankActivity.LIFE_TIME_6H, time_6h);
                editor.putInt(TankActivity.RETRY_COUNT, CONST.Tank.MAX_GAME_COUNT);
                editor.commit();
            }else {
                // TODO Not enough gold
                Log.d("PURCHASE TANKGRENADE", "Not enough gold");
            }


        }
        else if (id == R.id.buy_goldAd || id == R.id.goldAd2 || id == R.id.goldAd3 || id == R.id.goldAd4){

        }
        else if (id == R.id.buy_gold || id == R.id.gold2 || id == R.id.gold3){

        }
//                else if (id == R.id.stashTxt) {
//                    // TODO
//                }
        else if (id == R.id.watchBtn || id == R.id.watchgold) {
            if (activity instanceof TankActivity) {
                if (dialog instanceof TankEndGameDialog) {
                    ((TankActivity) activity).showRewardedVideo(this, (TankEndGameDialog) this.dialog);
                } else {
                    ((TankActivity) activity).showRewardedVideo(this);
                }
            } else if (activity instanceof TankMenuActivity) {
                ((TankMenuActivity) activity).showRewardedVideo(this);
            }
        }
        else if (id == R.id.buyCancelBtn) {
            if (activity instanceof TankActivity) {
                ((TankActivity) activity).updateBonusStack();
                mTankView.resumeNoAds();
            } else if (activity instanceof TankMenuActivity) {
                ((TankMenuActivity) activity).updateStore();
            }
            dismiss();
        }
        updateBonus();
    }

//    @Override
//    public void onClick(View view) {
//
//    }
}
