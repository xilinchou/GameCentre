package com.gamecentre.classicgames.tank;

import com.gamecentre.classicgames.model.Game;

public interface ServiceListener {
    void onServiceMessageReceived(int games, long time_left, boolean h6);
}
