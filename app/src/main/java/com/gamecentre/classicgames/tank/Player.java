package com.gamecentre.classicgames.tank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.gamecentre.classicgames.model.MTank;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;
import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

import java.util.ArrayList;

public class Player extends Tank{

    public int armour = 0;
    protected int frame;
    protected int frame_delay;
    public int lives = 3;
    protected int reloadTmr = (int)(0.1*TankView.TO_SEC);
    protected int reload_time = 0;
    protected int MaxBullet = 1;
    protected int starCount = 0;
    protected int direction;
    private int newDirection;
    private boolean clearBush = false;
    private boolean breakWall = false;
    private int level = 0;
    private  float bulletSpeed = 1;
    private boolean boat = false;
    public int[] kills;
    public int totalKills;
    public int totalScore;
    public int stageScore;
    boolean killed = false;



    public Player(ObjectType type, int x, int y, int player) {
        super(type, x, y, player);

        direction = CONST.Direction.UP;

        if(type == ObjectType.ST_PLAYER_1) {
            this.group = 5;
            super.x = (int)(4*(TankView.WIDTH/13));
            super.y = TankView.HEIGHT - h;
        }
        else {
            this.group = 6;
            super.x = (int)(8*(TankView.WIDTH/13));
            super.y = TankView.HEIGHT - h;
        }
        bulletSpeed = 1;
        shield = true;
        shieldTmr = ShieldTime/2;
        iceTmr = 0;
        kills = new int[]{0,0,0,0};
        ((TankActivity)(TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(lives));
        changeDirection(direction);
        totalScore = 0;
        stageScore = 0;
        totalKills = 0;
        frame = 0;
    }

    public void move() {
//        setDirection();
//        Log.d("MOTION", String.valueOf(moving) + " " + collision + " " + direction);
        if(respawn || destroyed) {
            return;
        }

        if((moving && !collision) || iceTmr > 0){
            getRect();
            if(freeze && freezeTmr > 0) {
                --freezeTmr;
                return;
            }
            SoundManager.playSound(Sounds.TANK.BACKGROUND,0.1f, 0);
            switch (direction) {
                case CONST.Direction.UP:
                    if(y <= 0)return;
                    y -= vy;
                    if(y < 0){
                        y = 0;
                    }
                    break;
                case CONST.Direction.DOWN:
                    if(y >= TankView.HEIGHT - h)return;
                    y += vy;
                    if(y > TankView.HEIGHT - h){
                        y = TankView.HEIGHT - h;
                    }

                    break;
                case CONST.Direction.LEFT:
                    if(x <= 0)return;
                    x -= vx;
                    if(x < 0) {
                        x = 0;
                    }
                    break;
                case CONST.Direction.RIGHT:
                    if(x >= TankView.WIDTH - w)return;
                    x += vx;
                    if(x > TankView.WIDTH - w) {
                        x = TankView.WIDTH - w;
                    }
                    break;
            }

        }
    }

    public boolean canClearBush() {
        return clearBush;
    }

    public boolean canBreakWall() {
        return breakWall;
    }

    public boolean hasBoat () {
        return boat;
    }

    public void setBoat(boolean boat) {
        this.boat = boat;
    }

    public boolean hasShield() {
        return shield;
    }

    public void setShield(boolean shield) {
        this.shield = shield;
        this.shieldTmr = ShieldTime;
    }

    public int getDirection() {
        return direction;
    }

    public void move(int dir) {
        if(iceTmr > 0) {
            newDirection = dir;
            moving = true;
            return;
        }
        if(dir != direction) {
            changeDirection(dir);
        }

        moving = true;

        int[] r = setCollissionRect(direction);
    }

    public void resetKills() {
        kills = new int[] {0,0,0,0};
        totalKills = 0;
    }

    public void changeDirection(int dir) {
        if(iceTmr > 0) {
            newDirection = dir;
            return;
        }
        direction = dir;
        newDirection = dir;
        int px_tile = (int) ((x / tile_x) * tile_x);
        int py_tile = (int) ((y / tile_y) * tile_y);

        if (x - px_tile < tile_x / TileScale) x = px_tile;
        else if (px_tile + tile_x - x < tile_x / TileScale) x = px_tile + tile_x;

        if (y - py_tile < tile_y / TileScale) y = py_tile;
        else if (py_tile + tile_y - y < tile_y / TileScale) y = py_tile + tile_y;
    }

    public boolean collidesWithObject(GameObjects targ) {
        rect = getRect();
        Rect tRect = targ.getRect();
        setCollissionRect(direction);
        if(Rect.intersects(cRct,tRect)) {
            collision = true;
        }
        else {
            collision = false;
        }
        return  collision;
    }



    public void stopMoving() {
        moving = false;
    }

    public void fire() {
//        Log.d("Tile", tile_x+" "+tile_y);
        if(!shooting || respawn || isDestroyed()) {
            return;
        }
        if(bullets.size() == MaxBullet) {
            return;
        }
        if(MaxBullet > 1 && reload_time > 0) {
            return;
        }

        int bx=0,by=0;
        switch (direction) {
            case CONST.Direction.UP:
                bx = x+(int) sprite.w/2;
                by = y;
                by = (int) ((by / tile_y) * tile_y) + tile_y;
                break;
            case CONST.Direction.DOWN:
                bx = x+(int) sprite.w/2;
                by = y+ sprite.h;
                by = (int) ((by / tile_y) * tile_y) - tile_y;
                break;
            case CONST.Direction.LEFT:
                bx = x;
                bx = (int) ((bx / tile_x) * tile_x) + tile_x;
                by = y+(int) sprite.h/2;
                break;
            case CONST.Direction.RIGHT:
                bx = x+ sprite.w;
                bx = (int) ((bx / tile_x) * tile_x) - tile_x;
                by = y+(int) sprite.h/2;
                break;
        }
        bId++;
        bullet.move(direction);
        bullet.setSpeed(bulletSpeed);
        bullet.setPlayer(true);
        bullet.id = bId;
        bullets.add((new Bullet(bullet,bx,by)));
        SoundManager.playSound(Sounds.TANK.FIRE);
        if(MaxBullet > 1){
            reload_time = reloadTmr;
        }
    }

//    public void setReloadTime(int t) {
//        ReloadTime = t;
//    }

    public void loseLife() {
        --lives;
        if(lives < 0) {
            lives = 0;
        }
        ((TankActivity)(TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(lives));
    }

    public boolean isAlive() {
        return lives > 0;
    }

    public void upgrade_armour() {
        armour++;
    }

    public void setDestroyed() {
        super.setDestroyed();
        killed = true;
        TankView.vibrate(500);
//        frame = 0;
//        frame_delay = dsprite.frame_time;
//        loseLife();
    }

    public void setFreeze() {
        TankView.freeze = true;
        TankView.freezeTmr = TankView.FreezeTime;
    }

    public void iceSlippage() {
        if(moving && !slip) {
            slip = true;
            iceTmr = IceTime;
        }
    }

    public void stopSlip() {
        iceTmr = 0;
        slip = false;
//        if(newDirection != direction){
//            changeDirection(newDirection);
//        }
        if(moving) {
            move(newDirection);
        }

    }

//    public boolean collidesWithTank(GameObjects targ) {
//        boolean c = super.collidesWithTank(targ);
//
//        return c;
//    }

    public int collidsWithBonus(Bonus b) {
        if(b.isAvailable() && super.collides_with(b)) {
            stageScore += 500;
            SoundManager.playSound(Sounds.TANK.BONUS, 1, 3);
            int bonus = b.getBonus();
            switch (bonus) {
                case Bonus.GRENADE:
                    break;
                case Bonus.HELMET:
                    shield = true;
                    shieldTmr = ShieldTime;
                    break;
                case Bonus.CLOCK:
                    break;
                case Bonus.SHOVEL:
                    break;
                case Bonus.TANK:
                    ++lives;
                    ((TankActivity)(TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(lives));
                    break;
                case Bonus.STAR:
                    ++starCount;
                    if(starCount > 3) {
                        clearBush = true;
                        starCount = 4;
                    }
                    if(starCount >= 3) {
                        breakWall = true;
                    }
                    bulletSpeed = 1.3f;
                    if(starCount >= 2) {
                        MaxBullet = 2;
                    }
                    armour++;
                    vx *= 1.2;
                    if(vx > DEFAULT_SPEED*1.35){
                        vx = DEFAULT_SPEED*1.35f;
                    }
                    vy *= 1.2;
                    if(vy > DEFAULT_SPEED*1.35){
                        vy = DEFAULT_SPEED*1.35f;
                    }
                    if(armour >= 3){
                        armour = 3;
                        level = 1;
                    }
                    break;
                case Bonus.GUN:
                    bulletSpeed = 1.3f;
                    breakWall = true;
                    vx *= 1.3;
                    vy *= 1.3;
                    if(vx > DEFAULT_SPEED*1.35){
                        vx = DEFAULT_SPEED*1.35f;
                    }
                    if(vy > DEFAULT_SPEED*1.35){
                        vy = DEFAULT_SPEED*1.35f;
                    }
                    starCount += 3;
                    if(starCount > 3) {
                        clearBush = true;
                        starCount = 4;
                    }
                    MaxBullet = 2;
                    armour = 3;
                    level = 1;

                    break;
                case Bonus.BOAT:
                    boat = true;
                    boatTmr = BoatTime;
                    break;
            }
            b.clearBonus();
            return bonus;
        }
        return -1;
    }

    public boolean collidsWithBullet(Bullet bullet) {
        if(isDestroyed()){
            return false;
        }
        if(super.collides_with(bullet)) {
            if(TankView.twoPlayers && this.player == 2) {
                bullet.recycle = true;
                return true;
            }

            if(shield) {
                bullet.setDestroyed(false);
                return true;
            }
            if(boat) {
                boat = false;
                bullet.setDestroyed(false);
                return true;
            }
            if(armour >= 3) {
                armour = 2;
                clearBush = false;
                breakWall = false;
                starCount = 2;
                bullet.setDestroyed();
                return true;
            }
            svrKill = TankView.twoPlayers && WifiDirectManager.getInstance().isServer();
            setDestroyed();
            bullet.setDestroyed();

            return true;
        }
        return false;
    }

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public void respawn() {
        if(!killed) {
            respawn(!killed);
            killed = false;
            return;
        }
        respawn = true;
        killed = false;
        starCount = 0;
        armour = 0;
        bulletSpeed = 1;
        clearBush = false;
        breakWall = false;
        destroyed = false;
        shield = true;
        vx = DEFAULT_SPEED;
        vy = DEFAULT_SPEED;
        MaxBullet = 1;
        shieldTmr = ShieldTime/2;
        direction = CONST.Direction.UP;
        if(this.type == ObjectType.ST_PLAYER_1) {
            x = (int) (4 * (TankView.WIDTH / 13));
        }
        else {
            x = (int)(8*(TankView.WIDTH/13));
        }
        y = TankView.HEIGHT - h;
        frame = 0;
        ((TankActivity)(TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(lives));
        changeDirection(direction);
    }

    public void respawn(boolean restart) {
        respawn = true;
        destroyed = false;
        shield = true;
        shieldTmr = ShieldTime/2;
        direction = CONST.Direction.UP;
        if(this.type == ObjectType.ST_PLAYER_1) {
            x = (int) (4 * (TankView.WIDTH / 13));
        }
        else {
            x = (int)(8*(TankView.WIDTH/13));
        }
        y = TankView.HEIGHT - h;
        frame = 0;
        ((TankActivity)(TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(lives));
        changeDirection(direction);
    }

    public void update() {
        if(MaxBullet > 1 && reload_time > 0) {
            --reload_time;
        }

        if(iceTmr > 0) {
            --iceTmr;
        }

        if(shield && shieldTmr > 0){
            --shieldTmr;
        }
        else {
            shield = false;
        }

//        if(respawn) {
//            destroyed = false;
//            direction = CONST.Direction.UP;
//            frame = 0;
//            respawn = false;
//        }

        for(int i = 0; i < bullets.size(); i++) {
            if(bullets.get(i).recycle) {
                bullets.set(i,null);
            }
        }

        // Remove all recycled bullets
        while(bullets.remove(null));

        fire();
        move();
        if(iceTmr == 1) {
            stopSlip();
        }
        for(Bullet bullet:bullets) {
            bullet.move();
        }
    }

    public void setModel(MTank model, float scale) {
        this.x = (int) (model.x * scale);
        this.y = (int) (model.y * scale);
        this.direction = model.dirction;
        this.setShield(model.shield);
        this.setBoat(model.boat);
        this.armour = model.armour;
        this.lives = model.lives;
        this.respawn = model.respawn;
        if(!destroyed && model.tDestroyed) {
            setDestroyed();
        }

        for(int i = 0; i < bullets.size(); i++) {
            if(!bullets.get(i).isDestroyed() || bullets.get(i).recycle) {
                bullets.set(i,null);
            }
        }

        while(bullets.remove(null));

//        for(int[] b:model.bullets) {
//            bullet.move(direction);
////            bullet.setSpeed(b[2]);
//            bullet.setPlayer(true);
//            if(b[3] == 1) {
//                bullet.setDestroyed();
//            }
//
//            bullets.add((new Bullet(bullet,(int)(b[0]* scale),(int)(b[1]* scale))));
//            bullet.destroyed = false;
//        }

        for (int i = 0; i < model.bullets.size(); i++) {
            boolean found = false;
            for(int j = 0; j < bullets.size(); j++) {
                if (model.bullets.get(i)[4] == bullets.get(j).id) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                if (model.bullets.get(i)[3] == 1) {
                    bullet.setDestroyed();
                }
                else{
                    bullet.destroyed = false;
                }


                bullet.id = model.bullets.get(i)[4];
                bullet.move(direction);
                bullet.setPlayer(false);
                bullets.add((new Bullet(bullet, (int) (model.bullets.get(i)[0] * scale), (int) (model.bullets.get(i)[1] * scale))));
                bullet.destroyed = false;
            }
        }
    }

    public void draw(Canvas canvas) {
        Bitmap bm;
        int bx, by, bw, bh;
        if(respawn) {
            if(frame >= spsprite.frame_count) {
                respawn = false;
                frame = 0;
                return;
            }
            canvas.drawBitmap(spbitmap[frame],x,y,null);
            if(frame_delay <= 0) {
                frame++;
                frame_delay = spsprite.frame_time;
            }
            else{
                --frame_delay;
            }
        }
        else if(!destroyed) {
            frame %= sprite.frame_count;
//            Log.d("DRAWP", sprite.frame_count+" "+typeVal+" "+frame);
            canvas.drawBitmap(TankView.tankBitmap.get(sprite.frame_count * armour + frame).get(4*group+direction),x,y,null);
            if(frame_delay <= 0) {
                frame = (frame + 1) % sprite.frame_count;
                frame_delay = sprite.frame_time;
            }
            else{
                --frame_delay;
            }

//            if(boat && boatTmr > 0) {
            if(boat) {
                mBoat.setPosition(x,y);
                mBoat.draw(canvas);
            }

            if(shield && shieldTmr > 0) {
                mShield.setPosition(x,y);
                mShield.draw(canvas);
            }
        }
        else {
            if (frame < dsprite.frame_count) {
                canvas.drawBitmap(dbitmap[frame], x - (int) (w / 2), y - (int) (h / 2), null);
                if(frame_delay <= 0) {
                    frame = frame + 1;
                    frame_delay = dsprite.frame_time;
                }
                else{
                    --frame_delay;
                }
            } else if (frame == dsprite.frame_count) {
                if(lives > 0) {
                    respawn();
                }
            }
        }

        for(Bullet bullet:bullets) {
            if(bullet != null) {
                bullet.draw(canvas);
            }
        }
    }
}
