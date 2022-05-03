package com.gamecentre.classicgames.tank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;
import com.gamecentre.classicgames.utils.CONST;

public class Enemy extends Tank{

    private Point target;
    private int change_dir_time;
    private int dir_time;
    private static boolean freeze = false, shield, boat;
    private static int freezeTmr, shieldTmr, boatTmr;
    public static int lives = 20;
    private  float bulletSpeed = 1;
    protected int reloadTmr = (int)(0.2*TankView.TO_SEC);
    protected int reload_time = 0;

    public Enemy(ObjectType type, int x, int y) {
        super(type, x, y, 0);
        target = new Point();
        target.x = TankView.WIDTH/2;
        target.y = TankView.HEIGHT/2;

        dir_time = 0;
        change_dir_time = (int)(Math.random()*30 + 10);
        direction = CONST.Direction.DOWN;
        if(type == ObjectType.ST_TANK_B) {
            this.vx += 3;
            this.vy += 3;
        }
        else if(type == ObjectType.ST_TANK_A) {
            this.vx -= 2;
            this.vy -= 2;
        }

        if(type == ObjectType.ST_TANK_C) {
            bulletSpeed = 1.5f;
            reloadTmr = (int)(0.8*TankView.TO_SEC);
        }
        else {
            reloadTmr = (int)(1*TankView.TO_SEC);
        }
//        moving = true;
    }

    public void setTarget(Point targ) {
        target = targ;
    }

    public void respawn() {
        respawn = true;
        destroyed = false;
        direction = CONST.Direction.DOWN;
        frame = 0;
    }

    public void setFreeze() {
        freeze = true;
        freezeTmr = FreezeTime;
    }

    public int getDirection() {
        return direction;
    }

    public void move() {
        if(respawn || destroyed) {
            return;
        }
        if(TankView.freeze) {
            return;
        }
        getRect();

        if(dir_time >= change_dir_time) {
            dir_time = 0;
            change_dir_time = (int)(Math.random()*30 + 10);

            float d = (float)Math.random();
            if(d < (type == ObjectType.ST_TANK_A ? 0.8 : 0.5) && target.x > 0 && target.y > 0) {
                int dx = (int)(target.x - x);
                int dy = (int)(target.y - y);

                d = (float)Math.random();

                if(Math.abs(dx) > Math.abs(dy))
                    direction = (d < 0.65) ? (dx < 0 ? CONST.Direction.LEFT : CONST.Direction.RIGHT) : (dy < 0 ? CONST.Direction.UP : CONST.Direction.DOWN);
                else
                    direction = (d < 0.65) ? (dy < 0 ? CONST.Direction.UP : CONST.Direction.DOWN) : (dx < 0 ? CONST.Direction.LEFT : CONST.Direction.RIGHT);
            }
            else {
                direction = (int)(Math.random()*4)%4;
            }

            int px_tile = (int)((x/tile_x)*tile_x);
            int py_tile = (int)((y/tile_y)*tile_y);

            if(x-px_tile < tile_x/TileScale) x = px_tile;
            else if(px_tile + tile_x - x < tile_x/TileScale) x = px_tile+tile_x;

            if(y-py_tile < tile_y/TileScale) y = py_tile;
            else if(py_tile + tile_y - y < tile_y/TileScale) y = py_tile+tile_y;
        }
        else {
            dir_time++;
        }


        switch (direction) {
            case CONST.Direction.UP:
                if(collision || rect.top <= 1)return;
                y -= vy;
                break;
            case CONST.Direction.DOWN:
                if(collision || rect.bottom >= TankView.HEIGHT)return;
                y += vy;
                break;
            case CONST.Direction.LEFT:
                if(collision || rect.left < 1)return;
                x -= vx;
                break;
            case CONST.Direction.RIGHT:
                if(collision || rect.right > TankView.WIDTH)return;
                x += vx;
                break;
        }
    }

    public void fire() {


        if(reload_time > 0 || TankView.freeze || bullets.size() == MaxBullet || isDestroyed()) {
            return;
        }
        int bx=0,by=0;
        switch (direction) {
            case CONST.Direction.UP:
                bx = x+(int) sprite.w/2;
                by = y;
                break;
            case CONST.Direction.DOWN:
                bx = x+(int) sprite.w/2;
                by = y+ sprite.h;
                break;
            case CONST.Direction.LEFT:
                bx = x;
                by = y+(int) sprite.h/2;
                break;
            case CONST.Direction.RIGHT:
                bx = x+ sprite.w;
                by = y+(int) sprite.h/2;
                break;
        }
        bullet.move(direction);
        bullet.setSpeed(bulletSpeed);
        bullet.setPlayer(false);
        bullets.add((new Bullet(bullet,bx,by)));
//        SoundManager.playSound(Sounds.TANK.FIRE);
        reload_time = (int)((0.2*TankView.TO_SEC) + Math.random()*reloadTmr);
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


    public void collidsWithBullet(Bullet bullet) {
        if(isDestroyed() || respawn) {
            return;
        }
        if(super.collides_with(bullet)) {
//            if(shield) {
//                bullet.setDestroyed();
//                return;
//            }
//            if(boat) {
//                boat = false;
//                bullet.setDestroyed();
//                return;
//            }
//            if(armour >= 3) {
//                armour = 2;
//                bullet.setDestroyed();
//                return;
//            }
            setDestroyed();
            bullet.setDestroyed();

        }
    }

    public boolean canBreakWall() {
        return false;
    }

    public boolean canClearBush() {
        return  false;
    }

    public void setDestroyed() {
        super.setDestroyed();
    }


    public void update() {
//        if(boat && boatTmr > 0){
//            --boatTmr;
//        }
//
//        if(shield && shieldTmr > 0){
//            --shieldTmr;
//        }

        if(reload_time > 0) {
            --reload_time;
        }

//        if(respawn) {
//            destroyed = false;
//            direction = CONST.Direction.DOWN;
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

        move();
        fire();
        for(Bullet bullet:bullets) {
            bullet.move();
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
                frame++;// = (frame + 1) % spsprite.frame_count;
                frame_delay = spsprite.frame_time;
            }
            else{
                --frame_delay;
            }
        }
        else if(!destroyed) {
            canvas.drawBitmap(TankView.tankBitmap.get(sprite.frame_count * typeVal + frame).get(4*group+direction),x,y,null);
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
        else if(!recycle) {
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
                super.recycle();
//                respawn();
            }
        }

        for(Bullet bullet:bullets) {
            if(bullet != null) {
                bullet.draw(canvas);
            }
        }
    }
}
