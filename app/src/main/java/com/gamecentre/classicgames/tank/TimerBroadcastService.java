package com.gamecentre.classicgames.tank;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.utils.RemoteMessageListener;

import java.text.SimpleDateFormat;

public class TimerBroadcastService extends Service {

    private final static String TAG = "BroadcastService";

    long time_left, life_time;
    int games;
    public static SharedPreferences settings;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            long currentTime = System.currentTimeMillis();

            long game6h = settings.getLong(TankActivity.LIFE_TIME_6H,0);
            if(game6h > 0 && game6h > currentTime) {
                life_time = game6h - currentTime;
                games = CONST.Tank.MAX_GAME_COUNT;
            }
            else {
                life_time = settings.getLong(TankActivity.LIFE_TIME, 0);
                games = settings.getInt(TankActivity.RETRY_COUNT, CONST.Tank.MAX_GAME_COUNT);
            }

//            Log.d("SERVICE","alive " + games + " " + life_time);

            if(life_time != 0 && games < CONST.Tank.MAX_GAME_COUNT) {
                currentTime = System.currentTimeMillis();
                time_left = (CONST.Tank.LIFE_DURATION_MINS * 60000) - ((currentTime - life_time) % (CONST.Tank.LIFE_DURATION_MINS * 60000));

                if (time_left < 1000) {
                    games++;
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt(TankActivity.RETRY_COUNT, games);
                    editor.putLong(TankActivity.LIFE_TIME, System.currentTimeMillis());
                    editor.commit();
                }
//                Log.d("SERVICE","sending message");
                MessageRegister.getInstance().registerServiceMessage(games,time_left);
            }
            else {
                MessageRegister.getInstance().registerServiceMessage(games,0);
            }

            timerHandler.postDelayed(this, 1000);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Starting timer...");

        timerHandler.postDelayed(timerRunnable, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
