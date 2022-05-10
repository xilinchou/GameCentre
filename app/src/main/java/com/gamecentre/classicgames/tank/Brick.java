package com.gamecentre.classicgames.tank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import com.gamecentre.classicgames.utils.CONST;

import java.util.ArrayList;

public class Brick extends GameObjects{
    Sprite sprite;
    Bitmap bitmap;
    ArrayList<Bitmap>d1bitmaps;
    ArrayList<Bitmap>d2bitmaps;
    Bitmap currentBitmap;
    int W, H;
    public int dstate = 0;
    public int drwx, drwy, dir;

    public Brick(int x, int y) {
        super(x, y);

        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_BRICK_WALL);
        bitmap = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y ,sprite.w,sprite.h);
        bitmap = Bitmap.createBitmap(bitmap,0,0,sprite.w,sprite.h);

        d1bitmaps = new ArrayList<>();
//        for(int i = 0; i < 4; i++) {
//            Bitmap bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.h+sprite.y+(i*sprite.h), sprite.w/2, sprite.h/2);
//            d1bitmaps.add(bm);
//        }
        d1bitmaps.add(Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y, sprite.w, sprite.h/2));
        d1bitmaps.add(Bitmap.createBitmap(TankView.graphics, sprite.x+sprite.w/2, sprite.y, sprite.w/2, sprite.h));
        d1bitmaps.add(Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y+sprite.h/2, sprite.w, sprite.h/2));
        d1bitmaps.add(Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y, sprite.w/2, sprite.h));

        d2bitmaps = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            Bitmap bm = Bitmap.createBitmap(TankView.graphics, sprite.x, 2*sprite.h+sprite.y+(i*sprite.h), sprite.w, sprite.h);
            d2bitmaps.add(bm);
        }
        currentBitmap = bitmap;

        super.w = sprite.w;
        super.h = sprite.h;
        super.x = x*sprite.w;
        super.y = y*sprite.h;
        W = super.w;
        H = super.h;
        getRect();
        drwx = super.x;
        drwy = super.y;
    }

    public void collidsWithBullet(Bullet bullet) {
        if(isDestroyed()) {
            return;
        }
        if(super.collides_with(bullet)) {
//            setDestroyed();
            bullet.setDestroyed();
        }
    }

    public boolean collidsWithBullet(int dir) {
        ++dstate;
        this.dir = dir;
        if(dstate > 1) {
            return  false;
        }
        if(dstate == 1){
            currentBitmap = d1bitmaps.get(dir);
        }
        if(dir == CONST.Direction.DOWN) {
            drwy += H/2;
        }
        else if(dir == CONST.Direction.RIGHT) {
            drwx += W/2;
        }
        return true;
    }

    public void draw(Canvas canvas) {
//        canvas.drawBitmap(currentBitmap,x,y,null);
        canvas.drawBitmap(currentBitmap,drwx,drwy,null);
    }
}
