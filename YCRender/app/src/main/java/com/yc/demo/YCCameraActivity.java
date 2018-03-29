package com.yc.demo;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yc.demo.render.egl.YCEngineEglWrap;
import com.yc.demo.render.gl.YCFbo;
import com.yc.demo.render.gl.YCTextureNode;
import com.yc.demo.render.gl.YCYuvNode;
import com.yc.demo.utils.YCCamera;
import com.yc.demo.video.process.MediaVideoManager;
import com.yc.demo.views.YCTextureViewSceneHandle;

public class YCCameraActivity extends PermissionActivity implements View.OnClickListener {

    private YCTextureViewSceneHandle mTextureViewSceneHandle = null;

    //views
    private TextView mTextView1 = null;

    //camera
    private YCCamera mRealCamera = null;
    private static final int[] mRequestCameraSize = new int[]{1280, 720};

    //gl
    private static final String SCENE_NAME = "MAIN_SCENE";
    private YCEngineEglWrap mEngineEglWrap = null;
    private YCYuvNode mYuvNode = null;
    private YCTextureNode mTextureNode = null;
    private YCFbo mFbo = null;

    @Override
    protected void onResume() {
        super.onResume();

        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaVideoManager != null) {
            mMediaVideoManager.destroy();
            mMediaVideoManager = null;
        }
        if (mEngineEglWrap != null) {
            mEngineEglWrap.destroy();
            mEngineEglWrap = null;
        }
        if (mTextureNode != null) {
            mTextureNode.destroy();
            mTextureNode = null;
        }
        if (mFbo != null) {
            mFbo.destroy();
            mFbo = null;
        }
    }

    private void init() {
        setContentView(R.layout.activity_camera);
        mTextView1 = findViewById(R.id.ycTextView1);
        findViewById(R.id.startBtn).setOnClickListener(this);
        findViewById(R.id.endBtn).setOnClickListener(this);

        TextureView mTextureView = findViewById(R.id.ycPreview1);
        mTextureViewSceneHandle = new YCTextureViewSceneHandle();
        mTextureViewSceneHandle.init(mTextureView);
        mTextureViewSceneHandle.setSurfaceTextureListener(new YCTextureViewSceneHandle.OnSurfaceTextureAvailable() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }
        });
    }

    //关闭摄像头
    public void closeCamera() {
        if (mRealCamera != null) {
            mRealCamera.stop();
            mRealCamera.destroy();
            mRealCamera = null;
        }
    }

    //开启相机
    public void openCamera() {
        if (mRealCamera != null && !mRealCamera.isPreviewing()) {
            closeCamera();
        }
        try {
            mRealCamera = new YCCamera();
            YCCamera.Config config = new YCCamera.Config();
            config.setPreviewSize(mRequestCameraSize[0], mRequestCameraSize[1]);
            config.setCameraId(YCCamera.Config.CAMERA_FACING_FRONT);
            mRealCamera.init(this, config);
            mRealCamera.open(new YCCamera.OnPreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mRealCamera.isPreviewing()) {
                        runEngine(data);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MediaVideoManager mMediaVideoManager = null;

    private void runEngine(byte[] data) {
        if (mTextureViewSceneHandle != null && mTextureViewSceneHandle.isEffective()) {
            if (mEngineEglWrap == null) {
                mEngineEglWrap = new YCEngineEglWrap(null);
                mEngineEglWrap.createScene(SCENE_NAME, mTextureViewSceneHandle.getSurfaceTexture());
                mFbo = YCFbo.create(mRequestCameraSize[0], mRequestCameraSize[1], 90);
                mYuvNode = YCYuvNode.create(mRequestCameraSize[0], mRequestCameraSize[1], 90);
                if (mYuvNode == null) {
                    throw new NullPointerException("YCYuvNode 创建失败！");
                }
                mYuvNode.setPreviewSize(mFbo.getWidth(), mFbo.getHeight());
                mTextureNode = YCTextureNode.create(mTextureViewSceneHandle.getTextureViewWidth(), mTextureViewSceneHandle.getTextureViewHeight());
                mTextureNode.setTextureId(mFbo.getTextureId());
                MediaVideoManager.Config config = new MediaVideoManager.Config();
                config.setEglContext(mEngineEglWrap.getEglContext());
                config.setInputTexId(mFbo.getTextureId());
                config.setVideoWidth(mFbo.getWidth());
                config.setVideoHeight(mFbo.getHeight());
                config.setVideoRotate(0);
                config.setVideoFileDir(Environment.getExternalStorageDirectory().getPath() + "/yc/dir");
                mMediaVideoManager = new MediaVideoManager();
                mMediaVideoManager.init(config);
                mMediaVideoManager.setOnProcessListener(new MediaVideoManager.OnProcessListener() {
                    @Override
                    public void onSuccess(String filePath, MediaVideoManager task) {
                        Log.e("Encoder", "  filePath = " + filePath);
                        mTextView1.setText("视频路径："+filePath);
                    }
                });
            }

            if (mEngineEglWrap.activeScene(SCENE_NAME)) {
                //绘制到FBO上
                mFbo.bind();
                mYuvNode.draw(data);
                mFbo.unbind();

                //绘制到屏幕上
                mTextureNode.draw();
                mEngineEglWrap.swapScene(SCENE_NAME);

                //录制视频
                if (mMediaVideoManager.isRecording()) {
                    mMediaVideoManager.updateFrame();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAuthorizeSuccess() {
        init();
    }

    @Override
    public void onAuthorizeFail() {

    }

    @Override
    protected String[] permissions() {
        return new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.READ_PHONE_STATE
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startBtn:
                boolean bool = mMediaVideoManager.start();
                if (bool) {
                    Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "录制失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.endBtn:
                mMediaVideoManager.stop();
                break;
            default:
                break;
        }
    }
}
