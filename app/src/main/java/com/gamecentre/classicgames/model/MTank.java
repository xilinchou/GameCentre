package com.gamecentre.classicgames.model;

import com.gamecentre.classicgames.tank.Bullet;
import com.gamecentre.classicgames.tank.Enemy;
import com.gamecentre.classicgames.tank.ObjectType;
import com.gamecentre.classicgames.tank.Player;
import com.gamecentre.classicgames.utils.CONST;

import java.io.Serializable;
import java.util.ArrayList;

public class MTank implements Serializable {
    public int x,y;
    public ObjectType type;
    public int dirction;
    public int armour;
    public int lives;
    public boolean boat,shield;
    public ArrayList<int[]>bullets;

    public  MTank(ObjectType type, int x, int y, int dirction) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.dirction = dirction;

    }

    public MTank(Player p) {
        x = p.x;
        y = p.y;
        type = p.type;
        dirction = p.getDirection();
        boat = p.hasBoat();
        shield = p.hasShield();
        armour = p.armour;
        lives = p.lives;


        bullets = new ArrayList<>();
        ArrayList<Bullet> pBullets = p.getBullets();
        for(Bullet b:pBullets) {
            if(b != null) {
                int[] bt = {b.x, b.y, b.getDirection(),b.isDestroyed()?1:0};
                bullets.add(bt);
            }
        }
    }


    public MTank(Enemy p) {
        x = p.x;
        y = p.y;
        type = p.type;
        dirction = p.getDirection();
//        boat = p.hasBoat();
//        shield = p.hasShield();
//        armour = p.armour;
        lives = p.lives;


        bullets = new ArrayList<>();
        ArrayList<Bullet> pBullets = p.getBullets();
        for(Bullet b:pBullets) {
            if(b != null) {
                int[] bt = {b.x, b.y,(int)b.getSpeed(), b.getDirection(),b.isDestroyed()?1:0};
                bullets.add(bt);
            }
        }
    }
}
