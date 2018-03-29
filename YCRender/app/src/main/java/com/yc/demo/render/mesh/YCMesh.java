package com.yc.demo.render.mesh;

import android.opengl.GLES20;

import com.yc.demo.render.gl.YCUtils;

/**
 * Created by lidm on 18/3/13.
 * 网格
 */
public class YCMesh {

    private float[] mVertexArray = null;
    private int[] mVertexIndicesArray = null;

    private int mVertexBufferId = -1;
    private int mVertexIndicesBufferId = -1;

    public void setVertexArray(float[] vertexArray) {
        mVertexArray = new float[vertexArray.length];
        System.arraycopy(vertexArray, 0, mVertexArray, 0, vertexArray.length);

        if (mVertexBufferId == -1) {
            mVertexBufferId = YCUtils.glGenBuffers(GLES20.GL_ARRAY_BUFFER, mVertexArray);
        } else {
            YCUtils.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mVertexBufferId, mVertexArray);
        }
    }

    public void setVertexIndicesArray(int[] vertexIndicesArray) {
        mVertexIndicesArray = new int[vertexIndicesArray.length];
        System.arraycopy(vertexIndicesArray, 0, mVertexIndicesArray, 0, vertexIndicesArray.length);

        if (mVertexIndicesBufferId == -1) {
            mVertexIndicesBufferId = YCUtils.glGenBuffers(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVertexIndicesArray);
        } else {
            YCUtils.glBufferSubData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVertexIndicesBufferId, mVertexIndicesArray);
        }
    }


    public int getVertexBufferId() {
        return mVertexBufferId;
    }

    public int getVertexIndicesBufferId() {
        return mVertexIndicesBufferId;
    }

    public float[] getVertexArray() {
        return mVertexArray;
    }

    public int[] getVertexIndicesArray() {
        return mVertexIndicesArray;
    }
}
