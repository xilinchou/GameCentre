package com.gamecentre.classicgames.pingpong;

import android.graphics.Rect;

import com.gamecentre.classicgames.model.Game;

import java.util.ArrayList;

public class PongGameModel extends Game {
//    private ArrayList<int[]> ball;
    private int[] paddle;
    private int[] ball;

    public PongGameModel(int bl, int bt, int br, int pl, int pt, int pr, int pb){
        paddle = new int[4];
        ball = new int[3];
        ball[0] = bl;
        ball[1] = bt;
        ball[2] = br;
        paddle[0] = pl;
        paddle[1] = pt;
        paddle[2] = pr;
        paddle[3] = pb;
    }

    public PongGameModel(){
        ball = new int[3];
        ball[0] = 0;
        ball[1] = 0;
        ball[2] = 0;
        paddle = new int[4];
        paddle[0] = 0;
        paddle[1] = 0;
        paddle[2] = 0;
        paddle[3] = 0;
    }

    public void setBall(int[] ball) {
        this.ball = ball;
    }

    public void setPaddle(int[] paddle) {
        this.paddle = paddle;
    }

    public int[] getPaddle() {
        return paddle;
    }

    public int[] getBall() {
//        float[] ball = {bx,by,br};

        return ball;
    }

//    @Override
//    public String toString() {
//        return "PongMsgSchema{"+
//                "bl="+ bx +
//                ",bt="+ by +
//                ",br="+ br +
//                ",pl="+ pl +
//                ",pt="+ pt +
//                ",pr="+ pr +
//                ",pb="+ pb +
//                "}";
//    }
}
