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
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;

import java.util.ArrayList;

public class TankDailyRewardDialog extends Dialog implements View.OnTouchListener{
    TankView mTankView;

    public AppCompatActivity activity;
    public Dialog dialog;
    public TankTextView watchBtn, cancelBtn;
    public ImageView watchBtn2;
    SharedPreferences settings;
    int goldCount;
    int day;

    ArrayList<RelativeLayout> rewardCover = new ArrayList<>();

    ArrayList<RelativeLayout> reward = new ArrayList<>();


    private static String TOTAL_GOLD = "TOTAL GOLD";

    public TankDailyRewardDialog(AppCompatActivity a, TankView mTankView) {
        super(a);
        this.activity = a;
        this.mTankView = mTankView;
    }

    public TankDailyRewardDialog(AppCompatActivity a, int day) {
        super(a);
        this.activity = a;
        this.mTankView = null;
        this.dialog = null;
        this.day = day;
    }

    public TankDailyRewardDialog(AppCompatActivity a, Dialog d) {
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

        setContentView(R.layout.activity_tank_daily_reward);
        watchBtn = (TankTextView) findViewById(R.id.watchRwdBtn);
        watchBtn2 = (ImageView) findViewById(R.id.watchRwdPlay);
        cancelBtn = (TankTextView) findViewById(R.id.closeReward);
        watchBtn.setOnTouchListener(this);
        watchBtn2.setOnTouchListener(this);
        cancelBtn.setOnTouchListener(this);

        rewardCover.add(findViewById(R.id.day1));
        rewardCover.add(findViewById(R.id.day2));
        rewardCover.add(findViewById(R.id.day3));
        rewardCover.add(findViewById(R.id.day4));
        rewardCover.add(findViewById(R.id.day5));
        rewardCover.add(findViewById(R.id.day6));
        rewardCover.add(findViewById(R.id.day7));

//        reward.add(findViewById(R.id.day1rwd));
//        reward.add(findViewById(R.id.day2rwd));
//        reward.add(findViewById(R.id.day3rwd));
//        reward.add(findViewById(R.id.day4rwd));
//        reward.add(findViewById(R.id.day5rwd));
//        reward.add(findViewById(R.id.day6rwd));
//        reward.add(findViewById(R.id.day7rwd));

        for(int i = 1; i <= 7; i++) {
            if(i > day) {
                break;
            }
            if(i <= day) {
                rewardCover.get(i-1).setAlpha(0);
                if(i == day) {
//                    reward.get(i-1).setVisibility(View.VISIBLE);
                }
                else{
//                    reward.get(i-1).setVisibility(View.INVISIBLE);
                }
            }
            else {
                rewardCover.get(i-1).setAlpha(0.5f);
//                reward.get(i-1).setVisibility(View.INVISIBLE);
            }
        }


//        goldCountTxt = (TankTextView)findViewById(R.id.stashTxt);
//
//        settings = activity.getSharedPreferences("TankSettings", 0);
//
//        goldCount = settings.getInt(TankActivity.GOLD, 0);
//        goldCountTxt.setText(String.format("x%s", goldCount));
    }


    @Override
    public boolean onTouch(View v, MotionEvent m) {
        {
            if(m.getAction() == MotionEvent.ACTION_DOWN){
//                int cost;
//                int goldCount = settings.getInt(TankActivity.GOLD,0);
                switch (v.getId()) {
                    case R.id.watchRwdBtn:
                    case R.id.watchRwdPlay:

//                        cost = Integer.parseInt((activity.getResources().getString(R.string.starboat_gold).replace("x","")));
//                        if(cost <= goldCount) {
//                            int star = settings.getInt(TankActivity.STAR,0);
//                            int boat = settings.getInt(TankActivity.BOAT,0);
//
//                            int amount = Integer.parseInt((activity.getResources().getString(R.string.starboat_count).replace("x","")));
//                            star += amount;
//                            boat += amount;
//                            goldCount -= cost;
//                            goldCountTxt.setText(String.format("x%s", goldCount));
//                            SharedPreferences.Editor editor = settings.edit();
//                            editor.putInt(TankActivity.STAR,star);
//                            editor.putInt(TankActivity.STAR,boat);
//                            editor.putInt(TankActivity.GOLD,goldCount);
//                            editor.apply();
//                            SoundManager.playSound(Sounds.TANK.BUY_ITEM);
//                        }
//                        else {
//                            // TODO Not enough gold
//                            Log.d("PURCHASE STARBOAT", "Not enough gold");
//                        }

                        break;

                    case R.id.closeReward:
                        dismiss();
                        break;
                    default:
                        break;
                }
            }
            return true;
        }
    }
}
