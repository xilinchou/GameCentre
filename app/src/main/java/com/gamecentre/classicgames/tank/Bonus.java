package com.gamecentre.classicgames.tank;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import com.gamecentre.classicgames.R;

import java.util.ArrayList;

public class Bonus extends GameObjects {

    private ArrayList<Bitmap> bitmaps;
    private Bitmap bonusBm;
    private Sprite sprite;
    private int bonus = -1;
    private int BLINKRATE = 10;
    private boolean on = false;
    private int blinkTmr = BLINKRATE;
    private int bonusTmr = 200;

    public static final int GRENADE = 0;
    public static final int HELMET = 1;
    public static final int CLOCK = 2;
    public static final int SHOVEL = 3;
    public static final int TANK = 4;
    public static final int STAR = 5;
    public static final int GUN = 6;
    public static final int BOAT = 7;

    public Bonus() {
        super(0,0);
        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_BONUS_GRENADE);
        super.w = sprite.w;
        super.h = sprite.h;

        bitmaps = new ArrayList<>();

        for(int i = 0; i < 8; i++) {
            Bitmap bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y+(i*sprite.h), sprite.w, sprite.h);
            bitmaps.add(bm);
        }
    }

    public void setBonus(int bonus) {
        if(bonusTmr > 0){
            --bonusTmr;
            return;
        }
        this.bonus = bonus;
        this.x = (int)(Math.random()*TankView.WIDTH- bitmaps.get(0).getWidth());
        this.y = (int)(Math.random()*TankView.HEIGHT- bitmaps.get(0).getHeight());
        if(bonus >= 0 && bonus <= 7) {
            bonusBm = bitmaps.get(bonus);
            on = true;
        }
        bonusTmr = 200;
    }

    public int getBonus() {
        return bonus;

    }

    public void clearBonus() {
        bonus = -1;
        on = false;
        bonusTmr = 200;
    }
    public boolean isOn() {
        return on;
    }
    public void draw(Canvas canvas) {
        if(bonus >= 0 && bonus <= 7) {
            if(blinkTmr > 0) {
                --blinkTmr;
            }
            else {
                on = !on;
                blinkTmr = BLINKRATE;
            }
            if(on) {
                if(x < 0) {
                    x = 0;
                }
                else if(TankView.WIDTH - x < (int)(sprite.w)) {
                    x = TankView.WIDTH - (int)(sprite.w);
                }

                if(y < 0) {
                    y = 0;
                }
                else if(TankView.HEIGHT - y < (int)(sprite.h)) {
                    y = TankView.HEIGHT - (int)(sprite.h);
                }
                canvas.drawBitmap(bonusBm, x, y, null);
            }
        }
    }
}
