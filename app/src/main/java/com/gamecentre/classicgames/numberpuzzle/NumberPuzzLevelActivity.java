package com.gamecentre.classicgames.numberpuzzle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.gamecentre.classicgames.activity.MenuActivity;
import com.gamecentre.classicgames.R;

public class NumberPuzzLevelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_number_puzz_level);

        RelativeLayout games = this.findViewById(R.id.level);
        int num_games = games.getChildCount();
        Log.d("number of games",String.valueOf(num_games));
        for(int i = 0; i < num_games; i++) {
            Button gamelevel = (Button)games.getChildAt(i);
            gamelevel.setOnClickListener(onClickListener);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.num_puzz3:
                    Log.d("Games","Number Puzzle 3");
                    level(3);
                    break;
                case R.id.num_puzz4:
                    Log.d("Games","Number Puzzle 4");
                    level(4);
                    break;
                case R.id.num_puzz5:
                    Log.d("Games","Number Puzzle 5");
                    level(5);
                    break;
                case R.id.num_puzz6:
                    Log.d("Games","Number Puzzle 6");
                    level(6);
                    break;
                default:
                    break;
            }
        }
    };

    public void level(int level) {
        Intent intent = new Intent(NumberPuzzLevelActivity.this, NumberPuzzleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("level",level);
        startActivity(intent);
        finish();
    }

    public void go_back(View view) {
        Intent intent = new Intent(NumberPuzzLevelActivity.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}