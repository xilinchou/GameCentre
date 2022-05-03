package com.gamecentre.classicgames.utils;

import com.gamecentre.classicgames.model.Game;

public interface RemoteMessageListener {
    void onMessageReceived(Game message);
}

