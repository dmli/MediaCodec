package com.yc.demo.utils;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.view.Surface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lidm on 18/1/2.
 * 异步相机
 */
public class YCCamera {

    //默认摄像头尺寸
    private static final int DEFAULT_CAMERA_WIDTH = 1280;
    private static final int DEFAULT_CAMERA_HEIGHT = 720;

    //Activity
    private Activity mActivity = null;

    //Surface
    private int[] mTextures = null;
    private SurfaceTexture mCameraSurfaceTexture = null;

    //Camera
    private Camera mCamera = null;
    private Config mConfig = null;
    private boolean isPreviewing = false;//是否预览中
    private OnPreviewCallback mPreviewCallback = null;
    private ExecutorService mThreadService = null;

    public void init(Activity activity) {
        init(activity, new Config());
    }

    public void init(Activity activity, Config config) {
        mConfig = config;
        mActivity = activity;
        mThreadService = Executors.newFixedThreadPool(1);
    }

    public void destroy() {
        mConfig = null;
        mActivity = null;
        mPreviewCallback = null;

        if (mThreadService != null) {
            mThreadService.shutdownNow();
            mThreadService = null;
        }

        if (mCameraSurfaceTexture != null) {
            mCameraSurfaceTexture.release();
            mCameraSurfaceTexture = null;
        }

        freeTexture();
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void switchCamera(int cameraId) {
        if (mConfig.mCameraId == cameraId) {
            return;
        }
        stop();
        mConfig.mCameraId = cameraId;
        open(mCameraSurfaceTexture, mPreviewCallback);
    }

    public void stop() {
        if (mCamera != null) {
            isPreviewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.addCallbackBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public boolean open() {
        if (mCameraSurfaceTexture == null) {
            mTextures = new int[1];
            GLES20.glGenTextures(1, mTextures, 0);
            mCameraSurfaceTexture = new SurfaceTexture(mTextures[0]);
        }
        return open(mCameraSurfaceTexture, null);
    }

    public boolean open(SurfaceTexture surfaceTexture) {
        return open(surfaceTexture, null);
    }

    public boolean open(OnPreviewCallback previewCallback) {
        if (mCameraSurfaceTexture == null) {
            mTextures = new int[1];
            GLES20.glGenTextures(1, mTextures, 0);
            mCameraSurfaceTexture = new SurfaceTexture(mTextures[0]);
        }
        return open(mCameraSurfaceTexture, previewCallback);
    }

    private boolean open(SurfaceTexture surfaceTexture, OnPreviewCallback previewCallback) {
        boolean result = false;
        try {
            mPreviewCallback = previewCallback;
            mCamera = Camera.open(mConfig.mCameraId);
            int orientation = getCameraDisplayOrientation(mConfig.mCameraId);
            mCamera.setDisplayOrientation(orientation);

            setParameters();
            if (previewCallback != null) {
                mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, final Camera camera) {
                        if (mThreadService == null) {
                            return;
                        }
                        camera.addCallbackBuffer(data);
                        if (mPreviewCallback != null) {
                            mPreviewCallback.onPreviewFrame(data, camera);
                            mThreadService.submit(new MKRunnable(data) {
                                @Override
                                public void run() {
                                    mPreviewCallback.onPreviewFrameAsync(mData, camera);
                                }
                            });
                        }
                    }
                });
                mCamera.addCallbackBuffer(new byte[mConfig.mPreviewWidth * mConfig.mPreviewHeight * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8]);
            }

            freeTexture();
            isPreviewing = true;
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
//            mCamera.stopPreview();
            mCamera.cancelAutoFocus();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            isPreviewing = false;
        }
        return result;
    }

    public int getCameraWidth() {
        return mConfig.mPreviewWidth;
    }

    public int getCameraHeight() {
        return mConfig.mPreviewHeight;
    }

    public boolean isPreviewing() {
        return isPreviewing;
    }

    //设置相机参数
    private void setParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        final List<String> focusModes = parameters.getSupportedFocusModes();
        // 连续聚焦
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        // 自动聚焦
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        //格式
        parameters.setPreviewFormat(mConfig.mPreviewFormat);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);


        //预览size
        Camera.Size CameraSize = findBestPreviewResolution(parameters);
        if (CameraSize != null) {
            parameters.setPreviewSize(CameraSize.width, CameraSize.height); //预览
            parameters.setPictureSize(CameraSize.width, CameraSize.height); //拍照
            mConfig.mPreviewWidth = CameraSize.width;
            mConfig.mPreviewHeight = CameraSize.height;
        }

        //设置帧率
        final int[] max_fps = findMaxPreviewFpsRange(parameters);
        if (max_fps != null) {
            parameters.setPreviewFpsRange(max_fps[0], max_fps[1]);
        }

        mCamera.setParameters(parameters);
    }

    //查找最大的预览帧率
    private int[] findMaxPreviewFpsRange(Camera.Parameters parameters) {
        final List<int[]> supportedFpsRange = parameters.getSupportedPreviewFpsRange();
        if (supportedFpsRange != null && supportedFpsRange.size() > 0) {
            int t_MaxFps = 0;
            int t_Index = 0;
            for (int i = 0; i < supportedFpsRange.size(); i++) {
                int[] ints = supportedFpsRange.get(i);
                if (ints[0] * ints[1] > t_MaxFps) {
                    t_MaxFps = ints[0] * ints[1];
                    t_Index = i;
                }
            }

            return supportedFpsRange.get(t_Index);
        }
        return null;
    }

    private int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    //找出最适合的预览界面分辨率
    private Camera.Size findBestPreviewResolution(Camera.Parameters mParams) {
        if (mParams != null) {
            Camera.Size defaultPreviewResolution = mParams.getPreviewSize();

            List<Camera.Size> rawSupportedSizes = mParams.getSupportedPreviewSizes();
            if (rawSupportedSizes == null) {
                return defaultPreviewResolution;
            }

            //精确查找一次
            List<Camera.Size> supportedPreviewResolutions = new ArrayList<>(rawSupportedSizes);
            for (Camera.Size resolution : supportedPreviewResolutions) {
                if (mConfig.mPreviewWidth == resolution.width && mConfig.mPreviewHeight == resolution.height) {
                    return resolution;
                }
            }
            //没有完全匹配的使用默认的size
            return defaultPreviewResolution;
        }
        return null;
    }

    //释放纹理
    private void freeTexture() {
        if (mTextures != null && mTextures[0] != 0) {
            GLES20.glDeleteTextures(1, mTextures, 0);
            mTextures = null;
        }
    }


    //MaConfig
    public static class Config {
        //相机前/后摄像头ID
        public static final int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
        public static final int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;

        //相机数据格式
        private int mPreviewFormat = ImageFormat.NV21;
        //是否前置摄像头 true前置  false后置
        private int mCameraId = CAMERA_FACING_FRONT;
        //预览size
        private int mPreviewWidth = DEFAULT_CAMERA_WIDTH;
        private int mPreviewHeight = DEFAULT_CAMERA_HEIGHT;

        public void setPreviewSize(int width, int height) {
            mPreviewWidth = width;
            mPreviewHeight = height;
        }

        //YCCamera.CAMERA_FACING_FRONT | YCCamera.CAMERA_FACING_BACK
        public void setCameraId(int cameraId) {
            mCameraId = cameraId;
        }

        //相机格式：ImageFormat.NV21 | ImageFormat.NV16...
        public void setPreviewFormat(int previewFormat) {
            this.mPreviewFormat = previewFormat;
        }

    }

    private abstract class MKRunnable implements Runnable {

        byte[] mData;

        MKRunnable(byte[] mData) {
            this.mData = mData;
        }
    }

    public abstract static class OnPreviewCallback implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
        }

        //异步
        public void onPreviewFrameAsync(byte[] data, final Camera camera) {

        }
    }
}
