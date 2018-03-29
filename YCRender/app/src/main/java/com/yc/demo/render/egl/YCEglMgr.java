package com.yc.demo.render.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by lidm on 17/10/26.
 * EGL相关类引用
 */

class YCEglMgr {

    //属性仅提供包内类使用
    private EGL10 mEgl10 = null;
    private EGLDisplay mEglDisplay = null;
    private EGLConfig mEglConfig = null;
    private EGLContext mEglContext = null;

    private final static int FM_EGL_EXTERNAL_FLAG = 0;//外部传入的
    private final static int FM_EGL_INTERNAL_FLAG = 1;//内部自己构建的
    private int mCurrentFlag;

    public void create(EGLContext eglContext) {
        this.mCurrentFlag = FM_EGL_EXTERNAL_FLAG;
        this.mEgl10 = (EGL10) EGLContext.getEGL();
        this.mEglDisplay = mEgl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        this.mEgl10.eglInitialize(mEglDisplay, version);
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = {
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 8,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };
        if (!mEgl10.eglChooseConfig(mEglDisplay, configSpec, configs, 1, configsCount)) {
            throw new RuntimeException(" eglChooseConfig failure！");
        } else if (configsCount[0] > 0) {
            this.mEglConfig = configs[0];
        }
        int[] attrib_list = {0x3098, 2, EGL10.EGL_NONE};
        this.mEglContext = mEgl10.eglCreateContext(mEglDisplay, mEglConfig, eglContext == null ? EGL10.EGL_NO_CONTEXT : eglContext, attrib_list);

        int[] qvalue = new int[1];
        this.mEgl10.eglQueryContext(mEglDisplay, mEglContext, 0x3098, qvalue);
    }

    //创建EGLSurface
    EGLSurface eglCreateWindowSurface(Object surfaceTexture) {
        EGLSurface eglSurface = mEgl10.eglCreateWindowSurface(mEglDisplay, mEglConfig, surfaceTexture, null);

        if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
            int error = mEgl10.eglGetError();
            YCLog.e("eglCreateWindowSurface failure!  " + error);
        } else {
            mEgl10.eglMakeCurrent(mEglDisplay, eglSurface, eglSurface, mEglContext);
            return eglSurface;
        }
        return null;
    }

    void glMakeCurrent(EGLSurface eglSurface) {
        if (eglSurface != null && eglSurface != EGL10.EGL_NO_SURFACE) {
            if (!mEgl10.eglMakeCurrent(mEglDisplay, eglSurface, eglSurface, mEglContext)) {
                int error = mEgl10.eglGetError();
                YCLog.e("eglMakeCurrent failure!  " + error);
            }
        }
    }

    //交换缓冲区，使用双缓冲时使用
    void eglSwapBuffers(EGLSurface eglSurface) {
        mEgl10.eglSwapBuffers(mEglDisplay, eglSurface);
    }

    public EGLContext getEglContext() {
        return mEglContext;
    }

    //销毁EGL相关资源
    void destroy() {
        if (mCurrentFlag == YCEglMgr.FM_EGL_INTERNAL_FLAG) {
            mEgl10.eglDestroyContext(mEglDisplay, mEglContext);
            mEgl10.eglTerminate(mEglDisplay);
        }

        mEglContext = null;
        mEglDisplay = null;
        mEglConfig = null;
        mEgl10 = null;
    }

    void eglDestroySurface(EGLSurface eglSurface) {
        if (eglSurface != null) {
            mEgl10.eglDestroySurface(mEglDisplay, eglSurface);
        }
    }


}
