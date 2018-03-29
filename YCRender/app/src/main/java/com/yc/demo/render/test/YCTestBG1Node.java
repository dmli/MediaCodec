package com.yc.demo.render.test;

import android.opengl.GLES20;

import com.yc.demo.render.gl.YCNode;
import com.yc.demo.render.gl.YCUtils;

/**
 * Created by lidm on 18/3/12.
 * 测试
 */
public class YCTestBG1Node extends YCNode {


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
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f
    };

    private final float[] mTextureArray = new float[]{
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    private int mVertexIndices[] = {
            0, 1, 3,
            1, 2, 3
    };

    private int mVertexBufferId = -1;
    private int mVertexIndexsBufferId = -1;
    private int mTextureBufferId = -1;
    private int mTextureId = -1;

    public static YCTestBG1Node create(int textureId) {
        YCTestBG1Node node = new YCTestBG1Node();
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
        mVertexIndexsBufferId = YCUtils.glGenBuffers(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVertexIndices);

        mTextureBufferId = YCUtils.glGenBuffers(GLES20.GL_ARRAY_BUFFER, mTextureArray);

    }

    public void draw() {
        if (mTextureId > 0) {
            bindProgram();

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

            //顶点
            YCUtils.subVertexAttribPointer(getShaderPositionId(), 2, mVertexBufferId);
            //纹理坐标
            YCUtils.subVertexAttribPointer(getShaderTextureCoordinateId(), 2, mTextureBufferId);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVertexIndexsBufferId);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, mVertexIndices.length, GLES20.GL_UNSIGNED_INT, 0);
        }
    }

}
