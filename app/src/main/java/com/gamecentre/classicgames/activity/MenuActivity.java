package com.gamecentre.classicgames.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.numberpuzzle.NumberPuzzLevelActivity;
import com.gamecentre.classicgames.pingpong.Pong;
import com.gamecentre.classicgames.tank.TankIntroActivity;

public class MenuActivity extends AppCompatActivity {
//    Sound sound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);

//        sound = new Sound();

        GridLayout games = this.findViewById(R.id.grid);
        int num_games = games.getChildCount();
        Log.d("number of games",String.valueOf(num_games));
        for(int i = 0; i < num_games; i++) {
            CardView gamecard = (CardView)games.getChildAt(i);
            gamecard.setOnClickListener(onClickListener);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sudoku:
                    Log.d("Games","Sudoku");
                    break;
                case R.id.tohanoi:
                    Log.d("Games","Tower of Hanoi");
                    break;
                case R.id.tetris:
                    Log.d("Games","Tetris");
                    break;
                case R.id.num_puzz:
                    num_puzz();
                    break;
                case R.id.pingpong:
                    ping_pong();
                    break;
                case R.id.watersort:
                    Log.d("Games","Water sort");
                    break;
                case R.id.tank:
                    Log.d("Games","Tank");
                    tank();
                    break;
                default:
                    break;
            }
        }
    };

    private void num_puzz() {
        Intent intent = new Intent(MenuActivity.this, NumberPuzzLevelActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    private void ping_pong() {
        Intent intent = new Intent(MenuActivity.this, Pong.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    private void tank() {
        Intent intent = new Intent(MenuActivity.this, TankIntroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    public void exitApp(View view) {
        super.onDestroy();
        finish();
    }
}