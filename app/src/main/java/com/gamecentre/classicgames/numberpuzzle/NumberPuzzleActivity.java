package com.gamecentre.classicgames.numberpuzzle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamecentre.classicgames.utils.Confetti;
import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class NumberPuzzleActivity extends AppCompatActivity implements View.OnTouchListener  {
    int level;
    BoardView boardView;
    LinearLayout boardrows;
    long startTime;
    Timer updateTimer;
    TextView timerView;
    int Rows;
    int Cols;
    boolean started = false;
    boolean ended = false;
    boolean sound = true;
    int resID;
    Confetti confetti;
    View[] confettiEmitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            level = (int)extras.getInt("level");
        }

        switch (level){
            case 3:
                Log.d("Level","3");
                setContentView(R.layout.activity_number_puzzle_3);
                resID = R.drawable.puzz3;
                break;
            case 4:
                Log.d("Level","4");
                setContentView(R.layout.activity_number_puzzle_4);
                resID = R.drawable.puzz4;
                break;
            case 5:
                Log.d("Level","5");
                setContentView(R.layout.activity_number_puzzle_5);
                resID = R.drawable.puzz5;
                break;
            case 6:
                Log.d("Level","6");
                setContentView(R.layout.activity_number_puzzle_6);
                resID = R.drawable.puzz6;
                break;
        }

        confettiEmitter = new View[4];
        confettiEmitter[0] = findViewById(R.id.emiter_top_right1);
        confettiEmitter[1] = findViewById(R.id.emiter_top_left1);
        confettiEmitter[2] = findViewById(R.id.emiter_top_left2);
        confettiEmitter[3] = findViewById(R.id.emiter_top_right2);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        timerView = this.findViewById(R.id.timerfield);

        Bitmap boardImg = BitmapFactory.decodeResource(this.getResources(),resID);
        boardView = new BoardView(this, boardImg, level, level);
        boardrows = this.findViewById(R.id.rows);

        SoundManager.getInstance();
        SoundManager.initSounds(this);
        int[] sounds = {R.raw.win1, R.raw.win2, R.raw.movecard, R.raw.wrongmove};
        SoundManager.loadSounds(sounds);

        update_board();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int pos = Integer.parseInt(String.valueOf(view.getTag()));
                int row = (int)(pos/Rows);
                int col = pos % Rows;
                int res = boardView.move_cellAt(row,col);
                if(res == 0 && sound) {
                    SoundManager.playSound(Sounds.NUM_PUZZ.MOVE);
                }
                else if (res == 1 && sound) {
                    SoundManager.playSound(Sounds.NUM_PUZZ.WRONGMOVE);
                }
                update_board();
        }
        return false;
    }

    public void reset(View view) {

        if (started) {

            if(ended) {
                if(confetti != null) {
                    confetti.cancel_confetti();
                }
                updateTimer.cancel();
                boardView.shuffle();
                start_timer();
                startTime = System.currentTimeMillis();
                update_board();
                ended = false;
            }
            else {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Really?");

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        updateTimer.cancel();
                        boardView.shuffle();
                        start_timer();
                        startTime = System.currentTimeMillis();
                        update_board();
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                alert.show();
            }
        }
        else{
            Rows = boardrows.getChildCount();
            for(int row = 0; row < Rows; row++) {
                LinearLayout current_row =  (LinearLayout)boardrows.getChildAt(row);
                Cols = current_row.getChildCount();
                for(int col = 0; col < Cols; col++) {
                    ImageView card = (ImageView) current_row.getChildAt(col);
                    card.setOnTouchListener(this);
                    card.setEnabled(true);
                    if(confetti != null) {
                        confetti.cancel_confetti();
                    }
                }
            }
            Button resetBtn = (Button)view;
            resetBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.reset));
            view.invalidate();

            boardView.shuffle();
            start_timer();
            startTime = System.currentTimeMillis();
            update_board();
            started = true;
        }
    }

    protected  void start_timer() {
        updateTimer = new Timer("update");
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                update_gametime();
            }
        }, 0, 1000);
    }

    protected void update_board() {
        for(int row = 0; row < boardrows.getChildCount(); row++) {
            LinearLayout current_row =  (LinearLayout)boardrows.getChildAt(row);
            for(int col = 0; col < current_row.getChildCount(); col++) {
                ImageView card = (ImageView) current_row.getChildAt(col);
                card.setBackground(boardView.updateCellAt(row,col));
            }
        }
        if(started) {
            boolean won = boardView.check_board(sound);
            if (won) {
                ended = true;
                if(sound) {
                    SoundManager.playSound(Sounds.NUM_PUZZ.WIN1);
                    SoundManager.playSound(Sounds.NUM_PUZZ.WIN2);
                }
                CharSequence text = "You Won!";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(this, text, duration);
                toast.show();
                updateTimer.cancel();
                confetti = new Confetti(this);
                confetti.generate_confetti(confettiEmitter);

                Rows = boardrows.getChildCount();
                for(int row = 0; row < Rows; row++) {
                    LinearLayout current_row =  (LinearLayout)boardrows.getChildAt(row);
                    Cols = current_row.getChildCount();
                    for(int col = 0; col < Cols; col++) {
                        ImageView card = (ImageView) current_row.getChildAt(col);
                        card.setEnabled(false);
                    }
                }
                started = false;
            }
        }
    }

    public void toggle_sound(View view) {
        Button soundBtn = (Button)view;
        if(sound){
            sound = false;
            soundBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nosound));
        }
        else{
            sound = true;
            soundBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sound));
        }
    }

    protected void update_gametime(){
        long currentTime = System.currentTimeMillis() - startTime;
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        timerView.setText(sdf.format(currentTime));
    }

    public void go_back(View view) {
        if (started) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Really? Your game will be lost!");

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Intent intent = new Intent(NumberPuzzleActivity.this, NumberPuzzLevelActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                }
            });

            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
            alert.show();
        }
        else{
            Intent intent = new Intent(NumberPuzzleActivity.this, NumberPuzzLevelActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }
    }

    protected void onDestroy()
    {
        super.onDestroy();
        SoundManager.cleanup();
    }
}