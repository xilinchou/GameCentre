package com.gamecentre.classicgames.tank;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gamecentre.classicgames.R;

import java.util.ArrayList;
import java.util.Collections;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link ConstructionFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class ConstructionFragment extends Fragment implements View.OnTouchListener {


    View rootView;
    LinearLayout stage;
    ImageView stone,brick,water,bush,ice,delObj;
    ImageView playStage,loadStage,saveStage;
    Drawable selectedObject = null;
    int newObjId = 1;
    int oldObjId = 1;

    int width, height, dim;

    ArrayList<int []> redoStack;
    int redoPointer;
    int pointerLimit;
    final int REDO_SIZE = 100;

    private String[][] stageObjects;

    public ConstructionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_construction, container, false);
        Log.d("Construction Fragment", "Fragment opened");
        stage = (LinearLayout) rootView.findViewById(R.id.stage);
        stage.setOrientation(LinearLayout.VERTICAL);
        stage.setOnTouchListener(this);

        stone = rootView.findViewById(R.id.stone);
        brick = rootView.findViewById(R.id.brick);
        bush = rootView.findViewById(R.id.bush);
        water = rootView.findViewById(R.id.water);
        ice = rootView.findViewById(R.id.ice);
        delObj = rootView.findViewById(R.id.delete);

        stone.setOnTouchListener(objSelectListener);
        brick.setOnTouchListener(objSelectListener);
        bush.setOnTouchListener(objSelectListener);
        water.setOnTouchListener(objSelectListener);
        ice.setOnTouchListener(objSelectListener);
        delObj.setOnTouchListener(objSelectListener);

        selectedObject = stone.getBackground();

        LinearLayout.LayoutParams vLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        vLayout.gravity = Gravity.CENTER;
        vLayout.weight = 1;
        vLayout.height = 0;


        LinearLayout.LayoutParams hLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        hLayout.gravity = Gravity.CENTER;
        hLayout.weight = 1;
        hLayout.width = 0;

        for(int row = 0; row < 26; row++) {

            LinearLayout rowObj = new LinearLayout(rootView.getContext());
            rowObj.setOrientation(LinearLayout.HORIZONTAL);
            rowObj.setLayoutParams(vLayout);
            for(int col = 0; col < 26; col++) {
                ImageView obj = new ImageView(rootView.getContext());
                obj.setLayoutParams(hLayout);
                if(row == 0 || row == 1) {
                    if(col == 0 || col == 1 || col == 12 || col == 13 || col == 24 || col == 25) {
                        obj.setBackgroundColor(Color.DKGRAY);
                    }
                }
                else if(row >= 23) {
                    if(col >= 11 && col <= 14) {
                        obj.setBackgroundColor(Color.DKGRAY);
                    }
                    if(row >= 24) {
                        if(col == 8 || col == 9 || col == 16 || col == 17) {
                            obj.setBackgroundColor(Color.DKGRAY);
                        }
                    }
                }
                rowObj.addView(obj);
            }
            stage.addView(rowObj);
        }

        rootView.findViewById(R.id.saveStage).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                }
                return true;
            }
        });

        rootView.findViewById(R.id.loadStage).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                }
                return true;
            }
        });

        rootView.findViewById(R.id.playStage).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                }
                return true;
            }
        });


        rootView.findViewById(R.id.backStage).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    Log.d("Fragment","Closing fragment");
                    if (fragmentManager.getBackStackEntryCount() > 0) {
                        fragmentManager.popBackStack();
                    }
                }
                return true;
            }
        });


        int [] init = new int[]{-1,-1,0,0};
        redoStack = new ArrayList<>(Collections.nCopies(REDO_SIZE,init));
        redoPointer = 0;
        pointerLimit = 0;

        stageObjects = new String[26][26];

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        stage.post(()->{
            int w = stage.getWidth();
            int h = stage.getHeight();
            Log.d("Resume Fragment", String.valueOf(width)+" "+height);

            int d = Math.min(w,h);
            d = (d/26)*26;

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)stage.getLayoutParams();

            params.width = d;
            params.height = d;
            stage.setLayoutParams(params);
            width = d;
            height = d;
            dim = (int)(width/26);
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("Touched stage", motionEvent.getX() + " " + motionEvent.getY() + " " + dim);
        int row = (int)(motionEvent.getY()/dim);
        int col = (int)(motionEvent.getX()/dim);
        if(row == 0 || row == 1) {
            if(col == 0 || col == 1 || col == 12 || col == 13 || col == 24 || col == 25) {
                return true;
            }
        }
        else if(row >= 23) {
            if(col >= 11 && col <= 14) {
                return true;
            }
            if(row >= 24) {
                if(col == 8 || col == 9 || col == 16 || col == 17) {
                    return true;
                }
            }
        }
        if(row < 26 && row >= 0 && col < 26 && col >= 0) {
            ImageView pos = (ImageView) ((LinearLayout)((LinearLayout)view).getChildAt(row)).getChildAt(col);
            pos.setBackground(selectedObject);
            updateStageObj(row,col,newObjId);
//            int[] stack = redoStack.get(redoPointer);
//            stack[0] = row;
//            stack[1] = col;
//            stack[2] = stack[3];
//            stack[3] = newObjId;
//            redoStack.set(redoPointer,stack);
//            redoPointer++;
//            redoPointer %= REDO_SIZE;
//            int limits = 0;
//            if(redoPointer > pointerLimit)
//            pointerLimit = (redoPointer-REDO_SIZE) % REDO_SIZE;
        }

        return true;
    }

    View.OnTouchListener objSelectListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                if(view.getId() == R.id.stone) {
                    newObjId = 1;
                }
                else if(view.getId() == R.id.brick) {
                    newObjId = 2;
                }
                else if(view.getId() == R.id.bush) {
                    newObjId = 3;
                }
                else if(view.getId() == R.id.water) {
                    newObjId = 4;
                }

                else if(view.getId() == R.id.ice) {
                    newObjId = 5;
                }
                else if(view.getId() == R.id.delete) {
                    newObjId = 0;
                }
//                else if(view.getId() == R.id.undo) {
//                    int pointer = redoPointer - 1;
//                    pointer %= REDO_SIZE;
//                    int[] stack = redoStack.get(pointer);
//                    if(stack[0] != -1 && stack[1] != -1)
//
//                    return true;
//                }
//                else if(view.getId() == R.id.redo) {
//                    return true;
//                }
//                else if(view.getId() == R.id.clear) {
//                    return true;
//                }

                ((RelativeLayout)stone.getParent()).setBackgroundColor(Color.TRANSPARENT);
                ((RelativeLayout)brick.getParent()).setBackgroundColor(Color.TRANSPARENT);
                ((RelativeLayout)bush.getParent()).setBackgroundColor(Color.TRANSPARENT);
                ((RelativeLayout)water.getParent()).setBackgroundColor(Color.TRANSPARENT);
                ((RelativeLayout)ice.getParent()).setBackgroundColor(Color.TRANSPARENT);
                ((RelativeLayout)delObj.getParent()).setBackgroundColor(Color.TRANSPARENT);

                RelativeLayout cursor = (RelativeLayout) view.getParent();
                cursor.setBackgroundColor(Color.YELLOW);
                selectedObject = view.getBackground();
            }
            return true;
        }
    };

    private Drawable getObject(int id) {
        Drawable d = null;
        switch(id) {
            case 1:
                d = stone.getBackground();
            case 2:
                d = brick.getBackground();
            case 3:
                d = bush.getBackground();
            case 4:
                d = water.getBackground();
            case 5:
                d = ice.getBackground();
        }
        return d;
    }

    private void updateStageObj(int row, int col, int id) {
        switch(id) {
            case 0:
                stageObjects[row][col] = null;
            case 1:
                stageObjects[row][col] = "@";
            case 2:
                stageObjects[row][col] = "#";
            case 3:
                stageObjects[row][col] = "%";
            case 4:
                stageObjects[row][col] = "~";
            case 5:
                stageObjects[row][col] = "-";
        }
    }
}