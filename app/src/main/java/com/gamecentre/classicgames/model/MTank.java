package com.gamecentre.classicgames.model;

import com.gamecentre.classicgames.tank.Bullet;
import com.gamecentre.classicgames.tank.Enemy;
import com.gamecentre.classicgames.tank.ObjectType;
import com.gamecentre.classicgames.tank.Player;
import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

import java.io.Serializable;
import java.util.ArrayList;

public class MTank implements Serializable {
    public int x,y;
    public ObjectType type;
    public int dirction;
    public int armour;
    public int lives;
    public boolean boat,shield;
    public boolean tDestroyed, respawn;
    public ArrayList<int[]>bullets;
    public int typeVal, group;
    public int id;
    public boolean hasBonus = false;
    public boolean svrKill = false;

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
        tDestroyed = p.isDestroyed();
        respawn = p.respawn;
        svrKill = p.svrKill;


        bullets = new ArrayList<>();
        ArrayList<Bullet> pBullets = p.getBullets();
        for(Bullet b:pBullets) {
            if(b.isDestroyed() && !b.svrKill && WifiDirectManager.getInstance().isServer()) {
                continue;
            }
            if(b != null) {
                int[] bt = {b.x, b.y, b.getDirection(),b.isDestroyed()?1:0,b.id};
                bullets.add(bt);
            }
        }
    }


    public MTank(Enemy e) {
        x = e.x;
        y = e.y;
        type = e.type;
        svrKill = e.svrKill;
        dirction = e.getDirection();
        tDestroyed = e.isDestroyed();
//        boat = p.hasBoat();
//        shield = p.hasShield();
//        armour = p.armour;
        lives = e.lives;
        typeVal = e.typeVal;
        group = e.group;
        respawn = e.respawn;
        id = e.id;
        hasBonus = e.hasBonus;




        bullets = new ArrayList<>();
        ArrayList<Bullet> pBullets = e.getBullets();
        for(Bullet b:pBullets) {
            if(b != null) {
                int[] bt = {b.x, b.y, b.getDirection(),b.isDestroyed()?1:0,b.id};
                bullets.add(bt);
            }
        }
    }
}
