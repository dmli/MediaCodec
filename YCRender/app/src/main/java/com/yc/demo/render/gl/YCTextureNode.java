package com.yc.demo.render.gl;

import android.opengl.GLES20;

/**
 * Created by lidm on 17/5/23.
 * 绘制RGBA纹理
 */

public class YCTextureNode extends YCNode {


    public static final String TEXTURE_VERTEX_SHADER = "attribute vec2 aPosition;" +
            "attribute vec2 aTextureCoordinate;" +
            "varying vec2 vTextureCoordinate;" +
            "uniform mat2 uRotation;" +
            "uniform vec2 uFlipScale;" +
            "void main() {" +
            "gl_Position = vec4(aPosition.x, aPosition.y, 0.0, 1.0);" +
            "vTextureCoordinate = aTextureCoordinate;" +
            "}";


    private static final String FRAGMENT_SHADER = "precision mediump float;" +
            "varying vec2 vTextureCoordinate;" +
            "uniform sampler2D inputImageTexture;" +
            "void main() {" +
            "lowp vec4 color = texture2D(inputImageTexture, vTextureCoordinate);" +
            "gl_FragColor = color.rgba;" +
            "}";

    private int mVertexBufferId;
    private int mTextureBufferId;
    private int mWidth;
    private int mHeight;
    private int mTextureId;

    private YCTextureNode() {

    }

    public static YCTextureNode create() {
        return create(0, 0);
    }

    public static YCTextureNode create(int width, int height) {
        YCTextureNode node = new YCTextureNode();
        node.mWidth = width;
        node.mHeight = height;
        if (node.init(TEXTURE_VERTEX_SHADER, FRAGMENT_SHADER)) {
            return node;
        }
        node.destroy();
        return null;
    }

    @Override
    protected void onBindShader() {
        GLES20.glUniform1i(GLES20.glGetUniformLocation(getProgramId(), "inputImageTexture"), 0);

        mVertexBufferId = createDefaultVertexBuffer();
        mTextureBufferId = createDefaultTextureBuffer();

        setFlipScale(1, 1);
    }


    public void setTextureId(int textureId) {
        this.mTextureId = textureId;
    }

    @Override
    public void draw() {
        if (mTextureId > 0) {
            bindProgram();
            if (mWidth != 0) {
                GLES20.glViewport(0, 0, mWidth, mHeight);
            }
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

            YCUtils.subVertexAttribPointer(getShaderPositionId(), 2, mVertexBufferId);
            YCUtils.subVertexAttribPointer(getShaderTextureCoordinateId(), 2, mTextureBufferId);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        }
    }
}
