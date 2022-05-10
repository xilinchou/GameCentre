package com.gamecentre.classicgames.model;

import android.graphics.Rect;

import com.gamecentre.classicgames.tank.Brick;
import com.gamecentre.classicgames.tank.Bush;
import com.gamecentre.classicgames.tank.Enemy;
import com.gamecentre.classicgames.tank.GameObjects;
import com.gamecentre.classicgames.tank.Ice;
import com.gamecentre.classicgames.tank.Player;
import com.gamecentre.classicgames.tank.StoneWall;
import com.gamecentre.classicgames.tank.TankView;
import com.gamecentre.classicgames.tank.Water;
import com.gamecentre.classicgames.utils.CONST;

import java.io.Serializable;
import java.util.ArrayList;

public class TankGameModel extends Game implements Serializable {
    public ArrayList<MTank> mEnemies = new ArrayList<>();
    public int[][] lObjects = new int[26][26];
    public MTank mPlayer;
    public int height = TankView.HEIGHT;
    public boolean gameOver = false;
    public boolean stageComplete = false;
    public boolean pause = false;
    public boolean end_game = false;
    public boolean eagleDestroyed;
    public boolean mlevelInfo = false;
    public int mlevel = 0;

    public void loadEnemies(ArrayList<Enemy> enemies) {
        for(Enemy enemy:enemies) {
//            mEnemies.add(new MTank(enemy.type,enemy.x,enemy.y,enemy.getDirection()));
            if(!enemy.recycle){
                mEnemies.add(new MTank(enemy));
            }
        }
    }

    public void loadPlayer(Player p) {
        mPlayer = new MTank(p);
    }

    public void loadLevelObjects(ArrayList<ArrayList<GameObjects>> lo) {
        for(int row = 0; row < lo.size(); row++) {
            for(int col = 0; col < lo.get(row).size(); col++) {
                if(lo.get(row).get(col) == null) {
                    lObjects[row][col] = 0;
                }
                else if(lo.get(row).get(col) instanceof StoneWall) {
                    lObjects[row][col] = 1;
                }
                else if(lo.get(row).get(col) instanceof Bush) {
                    lObjects[row][col] = 2;
                }
                else if(lo.get(row).get(col) instanceof Water) {
                    lObjects[row][col] = 3;
                }
                else if(lo.get(row).get(col) instanceof Ice) {
                    lObjects[row][col] = 4;
                }
                else if(lo.get(row).get(col) instanceof Brick) {
                    int dim = height/26;
                    Brick b = (Brick)lo.get(row).get(col);
                    Rect r = b.getRect();
                    if(b.dstate == 0) {
                        lObjects[row][col] = 5;
                    }
                    else if(b.dir == CONST.Direction.UP) {
                        lObjects[row][col] = 6;
                    }
                    else if (b.dir == CONST.Direction.DOWN){
                        lObjects[row][col] = 7;
                    }
                    else if(b.dir == CONST.Direction.LEFT) {
                        lObjects[row][col] = 8;
                    }
                    else if (b.dir == CONST.Direction.RIGHT){
                        lObjects[row][col] = 9;
                    }
                }
            }
        }
    }
}
