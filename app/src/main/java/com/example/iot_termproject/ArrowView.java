package com.example.iot_termproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

// 화살표를 그리는 Custom View 클래스
public class ArrowView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float initX, initY, radius;
    private boolean drawing = false;

    public ArrowView(Context context) {
        super(context);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.BLACK);
    }

}
