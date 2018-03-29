package com.yc.demo.render.egl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import java.util.Map;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by lidm on 17/10/26.
 * 提供了YCEglMgr和YCSurfaceMgr的管理，对外开放的
 */
public class YCEngineEglWrap {

    private YCEglMgr mFMEgl = null;
    private YCSurfaceMgr mSurfaceMgr = new YCSurfaceMgr();
    private YCEngineEglWrap.OnEngineRenderListener mEngineRenderListener = null;

    public YCEngineEglWrap(EGLContext glContext) {
        this.mFMEgl = new YCEglMgr();
        this.mFMEgl.create(glContext);
    }

    public void setEngineListener(YCEngineEglWrap.OnEngineRenderListener listener) {
        this.mEngineRenderListener = listener;
    }

    public void destroy() {
        _destroyGLScene();
        if (mFMEgl != null) {
            mFMEgl.destroy();
            mFMEgl = null;
        }
    }

    //创建一个GL场景
    public void createScene(String sceneName) {
        createScene(sceneName, null);
    }

    //创建一个GL场景
    public void createScene(String sceneName, Object surface) {
        //扔到循环队列中
        YCSurfaceMgr.FMSurface tSurface = new YCSurfaceMgr.FMSurface();
        if (surface == null) {
            tSurface.mType = 0;
            GLES20.glGenTextures(1, tSurface.mTextures, 0);
            tSurface.mSurface = new SurfaceTexture(tSurface.mTextures[0]);
        } else {
            tSurface.mType = 1;
            tSurface.mSurface = surface;
        }
        tSurface.mEGLSurface = mFMEgl.eglCreateWindowSurface(tSurface.mSurface);
        mSurfaceMgr.add(sceneName, tSurface);
    }

    //销毁所有场景
    private void _destroyGLScene() {
        if (mSurfaceMgr.size() <= 0) {
            return;
        }
        for (String sceneName : mSurfaceMgr.getSurfaceMap().keySet()) {
            YCSurfaceMgr.FMSurface tSurface = mSurfaceMgr.getSurfaceMap().get(sceneName);
            if (tSurface != null) {
                if (tSurface.mEGLSurface != null) {
                    mFMEgl.eglDestroySurface(tSurface.mEGLSurface);

                    tSurface.destroy();
                }
            }
        }
        mSurfaceMgr.clear();
    }

    //销毁场景
    public void destroyGLScene(String sceneName) {
        YCSurfaceMgr.FMSurface tSurface = mSurfaceMgr.remove(sceneName);
        if (tSurface != null) {
            if (tSurface.mEGLSurface != null) {
                mFMEgl.glMakeCurrent(tSurface.mEGLSurface);
                mFMEgl.eglDestroySurface(tSurface.mEGLSurface);
//                tSurface.destroy();
            }
        }
    }

    public boolean isExist(String sceneName) {
        return mSurfaceMgr.isExist(sceneName);
    }

    //激活场景
    public boolean activeScene(String sceneName) {
        YCSurfaceMgr.FMSurface tSurface = mSurfaceMgr.getSurface(sceneName);
        if (tSurface != null && tSurface.mEGLSurface != null) {
            mFMEgl.glMakeCurrent(tSurface.mEGLSurface);
            return true;
        }
        return false;
    }

    //交换场景
    public void swapScene(String sceneName) {
        YCSurfaceMgr.FMSurface tSurface = mSurfaceMgr.getSurface(sceneName);
        if (tSurface != null && tSurface.mEGLSurface != null) {
            mFMEgl.eglSwapBuffers(tSurface.mEGLSurface);
        }
    }

    public EGLContext getEglContext() {
        return mFMEgl.getEglContext();
    }


    void renderScene(String sceneName) {
        if (mEngineRenderListener == null) {
            return;
        }
        mEngineRenderListener.onEngineUpdate();
        if (activeScene(sceneName)) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            mEngineRenderListener.onEngineRender(sceneName);
            swapScene(sceneName);
        }
    }

    void renderSceneAll() {
        if (mEngineRenderListener == null) {
            return;
        }
        mEngineRenderListener.onEngineUpdate();
        final Map<String, YCSurfaceMgr.FMSurface> tSceneMap = mSurfaceMgr.getSurfaceMap();
        for (String sceneName : tSceneMap.keySet()) {
            if (activeScene(sceneName)) {
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                mEngineRenderListener.onEngineRender(sceneName);
                swapScene(sceneName);
            }
        }
    }


    public interface OnEngineRenderListener {

        void onEngineUpdate();

        void onEngineRender(String sceneName);
    }
}
