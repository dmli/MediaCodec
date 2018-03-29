package com.yc.demo.render.gl;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

/**
 * Created by lidm on 17/5/23.
 * 绘制RGBA纹
 */

public class YCOesNode extends YCNode {

    private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
            " precision mediump float;" +
            "varying vec2 sTextureCoordinate;" +
            "uniform samplerExternalOES inputImageTexture;" +
            "void main() {" +
            "lowp vec4 color = texture2D(inputImageTexture, sTextureCoordinate);" +
            "gl_FragColor = color;" +
            "}";

    private int vertexBufferId;
    private int textureBufferId;
    private int textureId;
    private int mWidth, mHeight;

    private YCOesNode() {

    }

    public static YCOesNode create(int mWidth, int mHeight) {
        YCOesNode node = new YCOesNode();
        node.mWidth = mWidth;
        node.mHeight = mHeight;
        if (node.init(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER)) {
            return node;
        }
        node.destroy();
        return null;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    @Override
    protected void onBindShader() {
        GLES20.glUniform1i(GLES20.glGetUniformLocation(getProgramId(), "inputImageTexture"), 0);
        vertexBufferId = createDefaultVertexBuffer();
        textureBufferId = createDefaultTextureBuffer();

        setFlipScale(1, 1);
    }

    @Override
    public void draw() {
        if (textureId > 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            bindProgram();
            GLES20.glViewport(0, 0, mWidth, mHeight);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

            YCUtils.subVertexAttribPointer(getShaderPositionId(), 2, vertexBufferId);
            YCUtils.subVertexAttribPointer(getShaderTextureCoordinateId(), 2, textureBufferId);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        }
    }
}
