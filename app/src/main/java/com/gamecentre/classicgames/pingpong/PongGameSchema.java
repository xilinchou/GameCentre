package com.gamecentre.classicgames.pingpong;

import com.gamecentre.classicgames.model.Game;

public class PongGameSchema extends Game {
    PongView.Paddle paddle;
    PongView.Ball ball;

    public PongGameSchema() {

    }

    public void setBall(PongView.Ball b) {
        ball = b;
    }

    public void setPaddle(PongView.Paddle p) {
        paddle = p;
    }

    public PongView.Paddle getPaddle() {
        return paddle;
    }

    public PongView.Ball getBall() {
        return ball;
    }

}
