package com.yc.demo.render.test;

import android.opengl.GLES20;

import com.yc.demo.render.gl.YCNode;
import com.yc.demo.render.gl.YCUtils;

/**
 * Created by lidm on 18/3/12.
 * 测试
 */
public class YCTestBG2Node extends YCNode {


    public static final String M_VERTEX_SHADER = "" +
            "attribute vec2 aPosition;" +
            "attribute vec2 aTextureCoordinate;" +
            "varying vec2 vTextureCoordinate;" +
            "uniform mat2 uRotation;" +
            "uniform vec2 uFlipScale;" +
            "void main() {" +
            "gl_Position = vec4(aPosition, 0.0, 1.0);" +
            "vTextureCoordinate = aTextureCoordinate;" +
            "}";


    public static final String M_FRAGMENT_SHADER = "" +
            "precision mediump float;" +
            "varying vec2 vTextureCoordinate;" +
            "uniform sampler2D inputImageTexture;" +
            "void main() {" +
            "gl_FragColor = texture2D(inputImageTexture, vTextureCoordinate);" +
            "}";

    private final float[] mVertexArray = new float[]{
            -1.0f, -0.464f,
            1.0f, -0.464f,
            1.0f, 0.1968f,
            -1.0f, 0.1968f
    };

    private final float[] mTextureArray = new float[]{
            1.0f, 0.73125f,
            0.0f, 0.73125f,
            0.0f, 0.4015625f,
            1.0f, 0.4015625f
    };

    private int mVertexBufferId;
    private int mTextureBufferId;
    private int mTextureId;

    public static YCTestBG2Node create(int textureId) {
        YCTestBG2Node node = new YCTestBG2Node();
        node.mTextureId = textureId;
        if (node.init(M_VERTEX_SHADER, M_FRAGMENT_SHADER)) {
            return node;
        }
        node.destroy();
        return null;
    }

    @Override
    protected void onBindShader() {
        mVertexBufferId = YCUtils.glGenBuffers(GLES20.GL_ARRAY_BUFFER, mVertexArray);
        mTextureBufferId = YCUtils.glGenBuffers(GLES20.GL_ARRAY_BUFFER, mTextureArray);

    }

    public void draw() {
        if (mTextureId > 0) {
            bindProgram();
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

            YCUtils.subVertexAttribPointer(getShaderPositionId(), 2, mVertexBufferId);
            YCUtils.subVertexAttribPointer(getShaderTextureCoordinateId(), 2, mTextureBufferId);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
            GLES20.glDisable(GLES20.GL_BLEND);
        }
    }

}
