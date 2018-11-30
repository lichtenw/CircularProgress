/*
    Copyright 2018 Brian Lichtenwalter - Circular Progress

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.lichtenw.android.circularprogress;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;


/**
 * A circular progress view implementation.  Tap to interact with the view 
 * to change the circular progress base and goal values.
 *
 * @author Brian Lichtenwalter
 */
public class ProgressCircleView extends View {


    private final List<NumberRect> goalRects = new ArrayList<NumberRect>();
    private final List<NumberRect> baseRects = new ArrayList<NumberRect>();
    private final Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect bounds = new Rect();
    private int base = 0;
    private int goal = 10;
    private int selectedGoal = 10;
    private NumberRect prevBaseRect, prevGoalRect;
    private int background, black, green, white;


    public ProgressCircleView(Context context) {
        this(context, null);
    }


    public ProgressCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rectPaint.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.circle_stroke));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        background = context.getResources().getColor(R.color.background);
        black = context.getResources().getColor(R.color.black);
        white = context.getResources().getColor(R.color.white);
        green = context.getResources().getColor(R.color.green);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressCircleView);
            base = a.getInteger(0, 0);
            goal = a.getInteger(1, 10);
            a.recycle();
        }
    }



    @Override 
    protected void onDraw(Canvas canvas) {

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float size = Math.min(width, height) * .75f;
        float left = width/2 - size/2;
        float top = height/2 - size/2;
        float right = left + size;
        float bottom = top + size;
        RectF rect = new RectF(left, top, right, bottom);

        if (goalRects.size() == 0) {
            //
            // Initialize selection rectangles...
            //
            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            float rsize = (float) (isPortrait ? width : height) / 11f;
            for (int i = 0, j = 0; i < 11; i++, j+=10) {
                if (isPortrait) {
                    baseRects.add(new NumberRect(j, i*rsize, 0, (i+1)*rsize, rsize));
                    goalRects.add(new NumberRect(j, i*rsize, height-rsize, (i+1)*rsize, height));
                } else {
                    baseRects.add(new NumberRect(j, 0f, i*rsize, rsize, (i+1)*rsize));
                    goalRects.add(new NumberRect(j, width-rsize, i*rsize, width, (i+1)*rsize));
                }
            }
            (prevBaseRect = baseRects.get(0)).selected = true;
            (prevGoalRect = goalRects.get(goal/10)).selected = true;
        }
        textPaint.setTextSize(goalRects.get(0).height() * .4f);

        //
        // fill background
        //
        //canvas.drawColor(background);
        //canvas.drawRect(0, 0, width, height, mTilePaint);
        //myBitmapDrawable.setTileModeX(Shader.TileMode.REPEAT); and myBitmapDrawable.setTileModeY(Shader.TileMode.REPEAT);

        //
        // render progress circle
        //
        float d = size * .05f;
        rect.inset(d, d);
        rectPaint.setColor(black);
        rectPaint.setStyle(Paint.Style.STROKE);
        canvas.drawOval(rect, rectPaint);

        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(green);
        rect.inset(-d, -d);
        float sweepAngle = (float) goal / 100f * 360f;
        canvas.drawArc(rect, 270, sweepAngle, true, rectPaint);

        d = size * .1f;
        rect.inset(d, d);
        rectPaint.setColor(background);
        canvas.drawOval(rect, rectPaint);

        //
        // render base rects...
        //
        for (NumberRect r : baseRects) {
            String num = String.valueOf(r.num);
            textPaint.getTextBounds(num, 0, num.length(), bounds);
            if (r.selected) {
                textPaint.setColor(0xFFEEEEEE);
                rectPaint.setColor(0xFF555555);
            } else {
                textPaint.setColor(0xFF555555);
                rectPaint.setColor(0xFFEEEEEE);
            }
            canvas.drawRect(r, rectPaint);
            canvas.drawText(num, r.centerX()-bounds.width()/2, r.centerY() + bounds.height()/2, textPaint);
        }

        //
        // render goal rects...
        //
        for (NumberRect r : goalRects) {
            String num = String.valueOf(r.num);
            textPaint.getTextBounds(num, 0, num.length(), bounds);
            if (r.selected) {
                textPaint.setColor(0xFF00FF16);
                rectPaint.setColor(0xFF555555);
            } else {
                textPaint.setColor(0xFF555555);
                rectPaint.setColor(0xFFEEEEEE);
            }
            canvas.drawRect(r, rectPaint);
            canvas.drawText(num, r.centerX()-bounds.width()/2, r.centerY() + bounds.height()/2, textPaint);
        }

        //
        // render current centered value...
        //
        String num = String.valueOf(goal);
        textPaint.setTextSize(rect.height() * .4f);
        textPaint.getTextBounds(num, 0, num.length(), bounds);
        textPaint.setColor(white);
        canvas.drawText(num, width/2-bounds.width()/2, height/2+bounds.height()/2, textPaint);
    }


    private final void hitDetection(float x, float y) {
        boolean found = false;
        for (NumberRect r : baseRects) {
            if (r.contains(x, y)) {
                if (prevBaseRect != null) {
                    prevBaseRect.selected = false;
                }
                prevBaseRect = r;
                r.selected = true;
                base = r.num;
                if (base > goal) {
                    prevGoalRect.selected = false;
                    prevGoalRect = goalRects.get(base/10);
                    prevGoalRect.selected = true;
                    selectedGoal = base;
                    goal = selectedGoal;
                }
                found = true;
                break;
            }
        }
        if (!found) {
            for (NumberRect r : goalRects) {
                if (r.contains(x, y)) {
                    if (prevGoalRect != null) {
                        prevGoalRect.selected = false;
                    }
                    prevGoalRect = r;
                    r.selected = true;
                    goal = r.num;
                    selectedGoal = goal;
                    if (base > goal) {
                        prevBaseRect.selected = false;
                        prevBaseRect = baseRects.get(goal/10);
                        prevBaseRect.selected = true;
                        base = goal;
                    }
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            startAnimation(new ProgressAnimation(ProgressCircleView.this));
        }
    }


    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                hitDetection(motionEvent.getX(), motionEvent.getY());
                break;
            }
        }
        return super.onTouchEvent(motionEvent);
    }



    /**
     * Custom rectangle to maintain selection state and number value.
     */
    static class NumberRect extends RectF {

        private int num;
        private boolean selected;

        private NumberRect(int num, float left, float top, float right, float bottom) {
            super(left, top, right, bottom);
            this.num = num;
        }
    }



    /**
     * Custom animation to handle circle progress.
     */
    static class ProgressAnimation extends Animation {

        private ProgressCircleView view;
        private float diff;

        public ProgressAnimation(ProgressCircleView view) {
            this.view = view;
            this.diff = view.selectedGoal - view.base;
            setInterpolator(new LinearInterpolator());
            setDuration(view.getContext().getResources().getInteger(R.integer.anim_duration));
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            int val = (int) Math.min(view.selectedGoal, (int) (view.base + (diff * interpolatedTime)));
            if (val != view.goal) {
                view.goal = val;
                view.invalidate();
            }
        }
    }

}
