package com.yc.demo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by lidm on 18/3/2.
 * 远点测试View
 */
public class YCPointTestView extends ImageView {

    private Rect rect = new Rect();
    private Paint paint = new Paint();
    private boolean isDraw = false;

    public YCPointTestView(Context context) {
        super(context);
        init();
    }

    public YCPointTestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public YCPointTestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int color = Color.rgb(255, 0, 0);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);

    }

    public void setRect(Rect rect) {
        this.rect.set(rect);
        isDraw = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isDraw) {
            canvas.save();
            canvas.drawRect(rect, paint);
            canvas.restore();
            isDraw = false;
        }
        super.onDraw(canvas);
        postDelayed(mDrawRunnable, 30);
    }

    private Runnable mDrawRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };
}
