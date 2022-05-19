package com.gamecentre.classicgames.tank;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.gamecentre.classicgames.utils.CONST;

import java.text.SimpleDateFormat;

public class TimerBroadcastService extends Service {

    private final static String TAG = "BroadcastService";

    public static final String LIFE_TIMER = "LIFE_TIMER";


    CountDownTimer cdt = null;
    long time_left, life_time;
    int games;
    SharedPreferences settings;
    Intent intent = new Intent(LIFE_TIMER);

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            life_time = settings.getLong(TankActivity.LIFE_TIME,0);
            games = settings.getInt(TankActivity.RETRY_COUNT,CONST.Tank.MAX_GAME_COUNT);

            if(life_time != 0 && games < CONST.Tank.MAX_GAME_COUNT) {
                long currentTime = System.currentTimeMillis();
                time_left = (CONST.Tank.LIFE_DURATION_MINS * 60000) - ((currentTime - life_time) % (CONST.Tank.LIFE_DURATION_MINS * 60000));

                if (time_left < 1000) {
                    games++;
//                    gameCountTxt.setText(String.valueOf(games));
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt(TankActivity.RETRY_COUNT, games);
                    editor.commit();
                    intent.putExtra("NEW_GAME", games);
                }
                intent.putExtra("NEW_TIME",time_left);

                sendBroadcast(intent);
//                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
//                gameCounter.setText(sdf.format(time_left));
            }

            timerHandler.postDelayed(this, 1000);
        }
    };

    public TimerBroadcastService(SharedPreferences settings){
        this.settings = settings;
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Starting timer...");

        timerHandler.postDelayed(timerRunnable, 0);
    }

    @Override
    public void onDestroy() {

        cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
