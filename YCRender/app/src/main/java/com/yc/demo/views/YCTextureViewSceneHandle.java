package com.yc.demo.views;

import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.ViewGroup;

/**
 * Created by lidm on 17/12/10.
 * 绘制窗口
 */
public class YCTextureViewSceneHandle {

    //小窗口，用来预览摄像头
    private TextureView mTextureView = null;
    private SurfaceTexture mSurfaceTexture = null;
    private int mTextureViewWidth = 0;
    private int mTextureViewHeight = 0;

    private OnSurfaceTextureAvailable mListener = null;

    public void setSurfaceTextureListener(OnSurfaceTextureAvailable listener) {
        this.mListener = listener;
    }

    public void destroy() {
        mSurfaceTexture = null;
        mTextureView = null;
    }

    public void init(TextureView _textureView) {
        mTextureView = _textureView;

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (mListener != null) {
                    mListener.onSurfaceTextureAvailable(surface, width, height);
                }
                mSurfaceTexture = surface;
                mTextureViewWidth = width;
                mTextureViewHeight = height;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                if (mListener != null) {
                    mListener.onSurfaceTextureSizeChanged(surface, width, height);
                }
                mSurfaceTexture = surface;
                mTextureViewWidth = width;
                mTextureViewHeight = height;
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mListener != null) {
                    mListener.onSurfaceTextureDestroyed(surface);
                }
                mSurfaceTexture = null;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                if (mListener != null) {
                    mListener.onSurfaceTextureUpdated(surface);
                }
                mSurfaceTexture = surface;
            }
        });
    }


    public void resetViewSize(int tarWidth, int tarHeight) {
        if (mTextureView == null) {
            return;
        }
        ViewGroup.LayoutParams lp = mTextureView.getLayoutParams();
        lp.width = tarWidth;
        lp.height = tarHeight;
        mTextureView.setLayoutParams(lp);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public int getTextureViewWidth() {
        return mTextureViewWidth;
    }

    public int getTextureViewHeight() {
        return mTextureViewHeight;
    }

    public boolean isEffective() {
        return mSurfaceTexture != null;
    }


    public static abstract class OnSurfaceTextureAvailable implements TextureView.SurfaceTextureListener {

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

}
