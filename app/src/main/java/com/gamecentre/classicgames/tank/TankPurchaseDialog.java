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
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;

public class TankPurchaseDialog extends Dialog implements View.OnTouchListener{
    TankView mTankView;

    public AppCompatActivity activity;
    public Dialog dialog;
    public TankTextView watchBtn, cancelBtn;
    SharedPreferences settings;
    int goldCount;
    TankTextView goldCountTxt;


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

        watchBtn = (TankTextView) findViewById(R.id.watchBtn);
        cancelBtn = (TankTextView) findViewById(R.id.buyCancelBtn);
        watchBtn.setOnTouchListener(this);
        findViewById(R.id.watchgold).setOnTouchListener(this);
        cancelBtn.setOnTouchListener(this);

        findViewById(R.id.starboat1).setOnTouchListener(this);
        findViewById(R.id.starboat2).setOnTouchListener(this);
        findViewById(R.id.starboat3).setOnTouchListener(this);
        findViewById(R.id.starboat4).setOnTouchListener(this);


        findViewById(R.id.clockshovel1).setOnTouchListener(this);
        findViewById(R.id.clockshovel2).setOnTouchListener(this);
        findViewById(R.id.clockshovel3).setOnTouchListener(this);
        findViewById(R.id.clockshovel4).setOnTouchListener(this);


        findViewById(R.id.gunhelmet1).setOnTouchListener(this);
        findViewById(R.id.gunhelmet2).setOnTouchListener(this);
        findViewById(R.id.gunhelmet3).setOnTouchListener(this);
        findViewById(R.id.gunhelmet4).setOnTouchListener(this);


        findViewById(R.id.tankgrenade1).setOnTouchListener(this);
        findViewById(R.id.tankgrenade2).setOnTouchListener(this);
        findViewById(R.id.tankgrenade3).setOnTouchListener(this);
        findViewById(R.id.tankgrenade4).setOnTouchListener(this);

        findViewById(R.id.gold1).setOnTouchListener(this);
        findViewById(R.id.gold1).setOnTouchListener(this);

        goldCountTxt = (TankTextView)findViewById(R.id.stashTxt);

        settings = activity.getSharedPreferences("TankSettings", 0);

        goldCount = settings.getInt(TankActivity.GOLD, 0);
        goldCountTxt.setText(String.format("x%s", goldCount));
    }


    @Override
    public boolean onTouch(View v, MotionEvent m) {
        {
            if(m.getAction() == MotionEvent.ACTION_DOWN){
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
                        goldCountTxt.setText(String.format("x%s", goldCount));
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
                        goldCountTxt.setText(String.format("x%s", goldCount));
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
                        goldCountTxt.setText(String.format("x%s", goldCount));
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
                        goldCountTxt.setText(String.format("x%s", goldCount));
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
                else if (id == R.id.stashTxt) {
                    // TODO
                }
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
                        ((TankMenuActivity) activity).updateBonus();
                    }
                    dismiss();
                }
            }
            return true;
        }
    }
}
