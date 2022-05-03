package com.gamecentre.classicgames.pingpong;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.pingpong.Pong;

public class SettingsDialog extends Dialog implements android.view.View.OnClickListener{
    RadioGroup strategyGroup;
    SeekBar aiSpeed;
    SeekBar ballSpeed;
    SeekBar paddleLives;
    CheckBox soundCheck;

    public AppCompatActivity activity;
    public Dialog d;
    public Button yes, no;
    SharedPreferences settings;

    public SettingsDialog(AppCompatActivity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
        yes = (Button) findViewById(R.id.saveBtn);
        no = (Button) findViewById(R.id.cancelBtn);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        settings = activity.getSharedPreferences("GameSettings", 0);
        loadStoredSettings(settings);


        strategyGroup =(RadioGroup)findViewById(R.id.select_strategy);
        aiSpeed =(SeekBar)findViewById(R.id.ai_speed_val);
        ballSpeed=(SeekBar)findViewById(R.id.ball_speed_val);
        paddleLives =(SeekBar)findViewById(R.id.paddle_lives_val);
        soundCheck = (CheckBox) findViewById(R.id.enableSound);
    }

    void loadStoredSettings(SharedPreferences settings) {
        String strategy = settings.getString(Pong.PREF_STRATEGY,"FOLLO");
        RadioButton button;
        if(strategy.contentEquals("PREDI")){
            button = (RadioButton)findViewById(R.id.predict);
            button.setChecked(true);
        }
        else if(strategy.contentEquals("EXACT")){
            button = (RadioButton)findViewById(R.id.exact);
            button.setChecked(true);
        }
        else if(strategy.contentEquals("FOLLO")){
            button = (RadioButton)findViewById(R.id.follow);
            button.setChecked(true);
        }
        boolean sound = settings.getBoolean(Pong.PREF_MUTED,true);
        CheckBox soundCheck = (CheckBox) findViewById(R.id.enableSound);
        soundCheck.setChecked(sound);

        int ai_speed = settings.getInt(Pong.PREF_HANDICAP,10);
        SeekBar ai = (SeekBar) findViewById(R.id.ai_speed_val);
        ai.setProgress(ai_speed);

        int ball_speed = settings.getInt(Pong.PREF_BALL_SPEED,10);
        SeekBar ball = (SeekBar) findViewById(R.id.ball_speed_val);
        ball.setProgress(ball_speed);

        int paddle_lives = settings.getInt(Pong.PREF_LIVES,3);
        SeekBar paddle = (SeekBar) findViewById(R.id.paddle_lives_val);
        paddle.setProgress(paddle_lives);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveBtn:
                SharedPreferences.Editor editor = settings.edit();

                int selectedId= strategyGroup.getCheckedRadioButtonId();
//                RadioButton strategy =(RadioButton)findViewById(selectedId);
//                Log.d("Strategy: ",strategy.getTag().toString());
                switch (selectedId){
                    case R.id.predict:
                        editor.putString(Pong.PREF_STRATEGY,"PREDI");
                        Log.d("Strategy: ","PREDI");
                        break;
                    case R.id.exact:
                        editor.putString(Pong.PREF_STRATEGY,"EXACT");
                        Log.d("Strategy: ","EXACT");
                        break;
                    case R.id.follow:
                        editor.putString(Pong.PREF_STRATEGY,"FOLLO");
                        Log.d("Strategy: ","FOLLOW");
                        break;
                }

                if(soundCheck.isChecked()) {
                    Log.d("Sound Check: ", "Sound on");
                    editor.putBoolean(Pong.PREF_MUTED,false);
                }
                else{
                    Log.d("Sound Check: ", "Sound off");
                    editor.putBoolean(Pong.PREF_MUTED,true);
                }

                int aiSpeedVal = aiSpeed.getProgress();
                editor.putInt(Pong.PREF_HANDICAP,aiSpeedVal);

                int ballSpeedVal = ballSpeed.getProgress();
                editor.putInt(Pong.PREF_BALL_SPEED,ballSpeedVal);

                int paddleLivesVal = paddleLives.getProgress();
                editor.putInt(Pong.PREF_LIVES,paddleLivesVal);

                Log.d("AI Speed: ", String.valueOf(aiSpeedVal));
                Log.d("Ball Speed: ", String.valueOf(ballSpeedVal));
                Log.d("Paddle Lives: ", String.valueOf(paddleLivesVal));
                editor.commit();
                dismiss();
                break;
            case R.id.cancelBtn:
                dismiss();
                break;
            default:
                break;
        }
    }
}
