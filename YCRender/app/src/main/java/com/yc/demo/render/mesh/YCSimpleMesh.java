package com.yc.demo.render.mesh;

/**
 * Created by lidm on 18/3/13.
 * 简单的 网格，绘制单张图使用
 */
public class YCSimpleMesh extends YCMesh {

    //左下角开始，逆时针一圈
    private static final float[] M_VERTEX_ARRAY = new float[]{
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f
    };

    private static final int M_VERTEX_INDICES_ARRAY[] = {
            0, 1, 2,
            0, 2, 3
    };

    private YCSimpleMesh() {
    }

    public static YCSimpleMesh create() {
        YCSimpleMesh mesh = new YCSimpleMesh();
        mesh.setVertexArray(M_VERTEX_ARRAY);
        mesh.setVertexIndicesArray(M_VERTEX_INDICES_ARRAY);
        return mesh;
    }


}
