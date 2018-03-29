package com.yc.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.yc.demo.render.egl.YCEngineEglWrap;
import com.yc.demo.render.gl.YCUtils;
import com.yc.demo.render.test.YCTestBG1Node;
import com.yc.demo.render.test.YCTestBG2Node;

/**
 * Created by lidm on 18/3/12.
 * 测试
 */
public class YCTestMeshActivity extends Activity implements View.OnClickListener {

    private static final String SCENE_NAME = "test";
    private TextureView mPreview1 = null;
    private YCEngineEglWrap mEngineEglWrap = null;
    private YCTestBG1Node mTestBG1Node = null;
    private YCTestBG2Node mTestBG2Node = null;
    private int mTextureId = -1;
    private int mTextureId2 = -1;
    private boolean isRender = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mEngineEglWrap != null){
            mEngineEglWrap.destroy();
            mEngineEglWrap = null;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mesh);


        initViews();
        setListener();
    }


    private void initViews() {
        int h = getResources().getDisplayMetrics().heightPixels;
        mPreview1 = findViewById(R.id.ycPreview1);
        ViewGroup.LayoutParams lp = mPreview1.getLayoutParams();
        lp.width = 720;
        lp.height = 1280;
        mPreview1.setLayoutParams(lp);

    }

    private void setListener() {
        findViewById(R.id.openBtn).setOnClickListener(this);
        findViewById(R.id.closeBtn).setOnClickListener(this);
        mPreview1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mEngineEglWrap = new YCEngineEglWrap(null);
                mEngineEglWrap.createScene(SCENE_NAME, surface);

                Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.bg1));
                Bitmap bitmap2 = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.bg2));
                mTextureId = YCUtils.createTextureId(bitmap);
                mTextureId2 = YCUtils.createTextureId(bitmap2);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                if (!bitmap2.isRecycled()) {
                    bitmap2.recycle();
                }
                mTestBG1Node = YCTestBG1Node.create(mTextureId);
                mTestBG2Node = YCTestBG2Node.create(mTextureId2);

                render();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void render() {
        //延时33毫秒，控制每秒最多渲染30帧，这里仅是渲染帧率(实际多数中低端机录制无法达到满30帧)
        mPreview1.postDelayed(mUpdateRunnable, 33);
    }

    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mEngineEglWrap != null) {
                if (mEngineEglWrap.activeScene(SCENE_NAME)) {
                    mTestBG1Node.draw();

                    if (isRender) {
                        mTestBG2Node.draw();
                    }
                    mEngineEglWrap.swapScene(SCENE_NAME);
                }
            }
            render();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openBtn:
                isRender = true;
                break;
            case R.id.closeBtn:
                isRender = false;
                break;
            default:
                break;
        }
    }
}
