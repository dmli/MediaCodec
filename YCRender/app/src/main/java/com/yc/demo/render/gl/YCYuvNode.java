package com.yc.demo.render.gl;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by ldm on 17/1/22.
 * 绘制YUV数据
 */

public class YCYuvNode extends YCNode {

    private static final String __TAG = YCYuvNode.class.getSimpleName();

    private static final String YUV_FRAGMENT_SHADER = "precision mediump float;" +
            "varying vec2 vTextureCoordinate;" +
            "uniform sampler2D tex_y;" +
            "uniform sampler2D tex_u;" +
            "void main (void){" +
            "mediump vec3 yuv;" +
            "lowp vec3 rgb;" +
            "vec4 c =vec4(texture2D(tex_y, vTextureCoordinate).r-16.0/255.0)*1.164;" +
            "vec4 u= vec4(texture2D(tex_u, vTextureCoordinate).a - 0.5);" +
            "vec4 v = vec4(texture2D(tex_u, vTextureCoordinate).r - 0.5);" +
            "c += v * vec4(1.596, -0.813, 0, 0);" +
            "c += u * vec4(0, -0.392, 2.017, 0);" +
            "c.a = 1.0;" +
            "gl_FragColor = c;" +
            "}";

    private int vertexBufferId;
    private int textureBufferId;
    private int yuvWidth, yuvHeight;
    private final int[] previewSize = new int[2];
    private int textureIdY, textureIdU;
    private float rotation;
    private ByteBuffer uvBuffer;

    public int getYuvWidth() {
        return yuvWidth;
    }

    public int getYuvHeight() {
        return yuvHeight;
    }

    /**
     * 创建YuvNode
     *
     * @param yuvWidth  宽度
     * @param yuvHeight 高度
     * @param rotation  角度
     * @return YCYuvNode ,创建失败返回null
     */
    public static YCYuvNode create(int yuvWidth, int yuvHeight, float rotation) {
        YCYuvNode yuvNode = new YCYuvNode(yuvWidth, yuvHeight, rotation);
        if (yuvNode.init(DEFAULT_VERTEX_SHADER, YUV_FRAGMENT_SHADER)) {
            return yuvNode;
        } else {
            yuvNode.destroy();
            Log.e(__TAG, "program initialization failed!");
        }
        return null;
    }

    /**
     * @param yuvWidth  宽度
     * @param yuvHeight 高度
     * @param rotation  角度
     */
    private YCYuvNode(int yuvWidth, int yuvHeight, float rotation) {
        this.yuvWidth = yuvWidth;
        this.yuvHeight = yuvHeight;
        this.rotation = rotation;

        this.uvBuffer = ByteBuffer.allocateDirect(((yuvWidth * yuvHeight) >> 1));
        this.uvBuffer.order(ByteOrder.nativeOrder()).position(0);
    }

    //设置预览窗口小大
    public void setPreviewSize(int width, int height) {
        this.previewSize[0] = width;
        this.previewSize[1] = height;
    }

    /**
     * 这个方法用来绑定shader中的属性
     * <p>
     * 例：
     * mPosition = glGetAttribLocation(glProgram->getProgramId(), "mPosition");
     * mRotation = GLES20.glGetUniformLocation(mProgram.getProgramId(), "mRotation");
     */
    @Override
    protected void onBindShader() {
        bindProgram();
        textureIdY = YCUtils.createBlankTexture2DId(yuvWidth, yuvHeight, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE);
        textureIdU = YCUtils.createBlankTexture2DId(yuvWidth >> 1, yuvHeight >> 1, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE);

        vertexBufferId = createDefaultVertexBuffer();
        textureBufferId = createDefaultTextureBuffer();

        setRotation(rotation);
        setFlipScale(1, 1);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(getProgramId(), "tex_y"), 0);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(getProgramId(), "tex_u"), 1);

    }

    @Override
    public void draw() {
    }

    public void draw(byte[] yuv) {
        ByteBuffer yuvBuffer = ByteBuffer.wrap(yuv);

        int start = yuvWidth * yuvHeight;
        int size = (yuvWidth * yuvHeight) >> 1;
        uvBuffer.position(0);
        uvBuffer.put(yuv, start, size);
        uvBuffer.position(0);

        bindProgram();
        GLES20.glViewport(0, 0, previewSize[0], previewSize[1]);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdY);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, yuvWidth, yuvHeight, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yuvBuffer.position(0));

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdU);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, yuvWidth >> 1, yuvHeight >> 1, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, uvBuffer);

        YCUtils.subVertexAttribPointer(getShaderPositionId(), 2, vertexBufferId);
        YCUtils.subVertexAttribPointer(getShaderTextureCoordinateId(), 2, textureBufferId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
//      GLES20.glFinish();
//      GLES20.glFlush();
    }


    /**
     * 释放数据
     */
    @Override
    public void destroy() {
        bindProgram();
        if (textureIdY != 0) {
            YCUtils.deleteTexture(textureIdY);
            textureIdY = 0;
        }
        if (textureIdU != 0) {
            YCUtils.deleteTexture(textureIdU);
            textureIdU = 0;
        }
        uvBuffer = null;
        super.destroy();
    }
}
