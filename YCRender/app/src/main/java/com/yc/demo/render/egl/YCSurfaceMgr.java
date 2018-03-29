package com.yc.demo.render.egl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by lidm on 17/10/26.
 * EGLSurface Set
 * 支持多个场景绑定
 */
class YCSurfaceMgr {

    //GL场景集合
    private final Map<String, FMSurface> mSurfaceMap = new HashMap<>();


    public Map<String, FMSurface> getSurfaceMap() {
        return mSurfaceMap;
    }

    public FMSurface remove(String key) {
        return mSurfaceMap.remove(key);
    }

    public FMSurface getSurface(String key) {
        return mSurfaceMap.get(key);
    }

    public int size() {
        return mSurfaceMap.size();
    }

    public void add(String key, FMSurface fmSurface) {
        mSurfaceMap.put(key, fmSurface);
    }

    public void clear() {
        mSurfaceMap.clear();
    }

    public boolean isExist(String sceneName) {
        return mSurfaceMap.containsKey(sceneName);
    }

    public static class FMSurface {
        public EGLSurface mEGLSurface;
        public int mType = 0;//0离线场景  1在线绘制
        int[] mTextures = new int[1];
        Object mSurface = null;

        void destroy() {
            if (mType == 0) {
                if (mTextures != null && mTextures[0] != 0) {
                    GLES20.glDeleteTextures(1, mTextures, 0);
                    mTextures = null;
                }
                if (mSurface != null) {
                    if (mSurface instanceof SurfaceTexture) {
                        ((SurfaceTexture) mSurface).release();
                    }
                    mSurface = null;
                }
            }
        }
    }

}
