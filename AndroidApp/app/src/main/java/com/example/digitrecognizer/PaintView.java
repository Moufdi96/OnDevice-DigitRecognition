package com.example.digitrecognizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;

public class PaintView extends View {
    private ViewGroup.LayoutParams mParams;
    public android.graphics.Path mPath = new Path();
    private Canvas mCanvas;
    private Paint brush = new Paint();
    private Paint brushErase = new Paint();

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.brush.setAntiAlias(true);
        this.brush.setColor(Color.parseColor( "#953929"));
        this.brush.setStyle(Paint.Style.STROKE);
        this.brush.setStrokeJoin(Paint.Join.ROUND);
        this.brush.setStrokeWidth(55.0f);

        //this.mParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(pointX,pointY);
                return true;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(pointX,pointY);
                break;
            default:
                return false;
        }
        postInvalidate();
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas = canvas;
        mCanvas.drawPath(mPath, brush);
    }

    public void clearView(){
        this.mPath.reset();
        this.destroyDrawingCache();
        this.invalidate();
    }

}
