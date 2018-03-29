package com.yc.demo.render.gl;

import android.opengl.GLES20;

/**
 * Created by lidm on 17/8/7.
 * glFrameBuffer
 */

public class YCFbo {

    private int mTextureId = 0;
    private int mFboId = 0;
    private final int[] mFboSize = new int[2];
    private int mAngle = 0;

    private YCFbo() {
    }

    public static YCFbo create(int width, int height, int angle) {
        YCFbo fbo = new YCFbo();
        if (fbo.init(width, height, angle)) {
            return fbo;
        }
        return null;
    }

    private boolean init(int width, int height, int angle) {
        mAngle = angle;
        if (angle == 90 || angle == 270) {
            mFboSize[0] = height;
            mFboSize[1] = width;
        } else {
            mFboSize[0] = width;
            mFboSize[1] = height;
        }
        mTextureId = YCUtils.createBlankTexture2DId(mFboSize[0], mFboSize[1], GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE);

        int fboIds[] = new int[1];
        GLES20.glGenFramebuffers(1, fboIds, 0);
        mFboId = fboIds[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId, 0);
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            YCUtils.deleteTexture(mTextureId);
            return false;
        }
        return true;
    }

    //FBO绑定的textureId
    public int getTextureId() {
        return mTextureId;
    }

    public int getWidth() {
        return mFboSize[0];
    }

    public int getHeight() {
        return mFboSize[1];
    }

    public int getAngle() {
        return mAngle;
    }

    //绑定当前的FBO
    public void bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId);
    }

    //释放绑定的FBO
    public void unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    //销毁
    public void destroy() {
        if (mFboId != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, 0, 0);

            YCUtils.deleteFrameBuffers(mFboId);
        }
    }
}
