package com.yc.demo.video.process;

import android.text.TextUtils;
import android.util.Log;

import com.yc.demo.render.egl.YCEngineEglWrap;
import com.yc.demo.render.gl.YCTextureNode;
import com.yc.demo.video.encoder.MediaAudioEncoder;
import com.yc.demo.video.encoder.MediaMuxerWrapper;
import com.yc.demo.video.encoder.MediaVideoEncoder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by hopeliao on 2017/8/31.
 * 视频录制
 */
public class MediaVideoManager implements Runnable {
    private static final String __TAG = "Encoder";
    private MediaMuxerWrapper mMuxer = null;
    private String mVideoFilePath;
    private MediaVideoManager.Config mConfig = null;
    private OnProcessListener mOnProcessListener = null;
    private MediaVideoEncoder mVideoEncoder = null;

    //EGL：用来填充数据
    private YCEngineEglWrap mEngineEglWrap = null;
    private YCTextureNode mTextureNode = null;
    private final String RECORD_SCENE_NAME = "MediaMuxerScene";

    private boolean isRecording = false;
    private boolean isDestroy = false;

    //Task
    private final int TASK_RECORD_START = 1;
    private final int TASK_RECORD_RUN = 2;
    private final int TASK_RECORD_STOP = 0;
    private final List<Integer> mTask = new ArrayList<>();

    public boolean isRecording() {
        return isRecording;
    }

    public MediaVideoManager() {
    }


    public void init(Config config) {
        this.mConfig = config;
        new Thread(this, getClass().getName()).start();
    }

    public void destroy() {
        _stop();
        isDestroy = true;
    }

    public void setOnProcessListener(OnProcessListener listener) {
        this.mOnProcessListener = listener;
    }

    public void updateFrame() {
        if (mMuxer != null) {
            _sendRunMessage();
            mMuxer.frameAvailableSoon();
        }
    }

    public boolean start() {
        try {
            mVideoFilePath = makeVideoFile();
            Log.e(__TAG, "mVideoFilePath = " + mVideoFilePath);
            if (mVideoFilePath == null) {
                return false;
            }
            mMuxer = new MediaMuxerWrapper(mVideoFilePath);
            mVideoEncoder = new MediaVideoEncoder(mMuxer, mConfig.mVideoWidth, mConfig.mVideoHeight);
            new MediaAudioEncoder(mMuxer);

            mMuxer.updateRotate(mConfig.mVideoRotate);
            mMuxer.prepare();
            mMuxer.startRecording();
            isRecording = true;
            _sendStartMessage();
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void stop() {
        isRecording = false;
        _stop();

        if (mOnProcessListener != null) {
            mOnProcessListener.onSuccess(mVideoFilePath, this);
        }
    }

    private void _stop() {
        _sendStopMessage();
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
        }
    }

    //通知渲染线程开始绘制
    private void _sendStartMessage() {
        synchronized (mTask) {
            mTask.add(TASK_RECORD_START);
            mTask.notify();
        }
    }

    //通知渲染线程停止绘制
    private void _sendStopMessage() {
        synchronized (mTask) {
            mTask.add(TASK_RECORD_STOP);
            mTask.notify();
        }
    }

    //通知渲染线程可以正常渲染
    private void _sendRunMessage() {
        synchronized (mTask) {
            mTask.add(TASK_RECORD_RUN);
            mTask.notify();
        }
    }

    @Override
    public void run() {
        //创建EGL
        if (mEngineEglWrap == null) {
            mEngineEglWrap = new YCEngineEglWrap(mConfig.mEglContext);
        }
        while (!isDestroy) {
            synchronized (mTask) {
                if (mTask.size() > 0) {
                    int tag = mTask.remove(0);
                    switch (tag) {
                        case TASK_RECORD_START:
                        case TASK_RECORD_RUN:
                            _draw();
                            break;
                        case TASK_RECORD_STOP:
                            mEngineEglWrap.destroyGLScene(RECORD_SCENE_NAME);
                            break;
                        default:
                            break;
                    }
                } else {
                    try {
                        mTask.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //销毁OpenGL相关资源
        if (mEngineEglWrap != null) {
            mEngineEglWrap.destroy();
            mEngineEglWrap = null;
        }
        if (mTextureNode != null) {
            mTextureNode.destroy();
            mTextureNode = null;
        }
    }

    private void _draw() {
        long time1 = System.currentTimeMillis();
        if (mEngineEglWrap.isExist(RECORD_SCENE_NAME)) {
            if (mEngineEglWrap.activeScene(RECORD_SCENE_NAME)) {
                mTextureNode.draw();
                mEngineEglWrap.swapScene(RECORD_SCENE_NAME);
            }
        } else {
            if (mVideoEncoder != null && mVideoEncoder.getSurface() != null) {
                mEngineEglWrap.createScene(RECORD_SCENE_NAME, mVideoEncoder.getSurface());
                mTextureNode = YCTextureNode.create(mConfig.mVideoWidth, mConfig.mVideoHeight);
                if (mTextureNode == null) {
                    Log.e(__TAG, "YCTextureNode.create(" + mConfig.mVideoWidth + ", " + mConfig.mVideoHeight + ") 创建失败！");
                } else {
                    mTextureNode.setTextureId(mConfig.mInputTexId);
                }
            }
        }
        Log.e(__TAG, "_draw time = " + (System.currentTimeMillis() - time1));
    }

    private String makeVideoFile() {
        //检查路径
        File f = new File(mConfig.mVideoFileDir);
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                Log.e(__TAG, "视频路径无法创建 [" + mConfig.mVideoFileDir + "]");
                return null;
            }
        }
        //创建文件
        if (TextUtils.isEmpty(mConfig.mVideoFileName)) {
            mConfig.mTempFileName = "v_" + System.currentTimeMillis() + ".mp4";
        } else {
            f = new File(mConfig.mVideoFileDir + File.separator + mConfig.mVideoFileName);
            if (f.exists() && !f.delete()) {
                Log.e(__TAG, "视频文件已经存且删除失败！" + f.getAbsolutePath());
                return null;
            }
            mConfig.mTempFileName = mConfig.mVideoFileName;
        }
        f = new File(mConfig.mVideoFileDir + File.separator + mConfig.mTempFileName);
        try {
            if (!f.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return f.getAbsolutePath();
    }

    public interface OnProcessListener {
        void onSuccess(String filePath, MediaVideoManager task);
    }

    public static class Config {
        int mVideoWidth;
        int mVideoHeight;
        int mVideoRotate;
        String mVideoFileDir;
        String mVideoFileName;
        EGLContext mEglContext;
        int mInputTexId;

        private String mTempFileName;//内部使用的临时名称

        public void setVideoWidth(int videoWidth) {
            this.mVideoWidth = videoWidth;
        }

        public void setVideoHeight(int videoHeight) {
            this.mVideoHeight = videoHeight;
        }

        public void setVideoRotate(int videoRotate) {
            this.mVideoRotate = videoRotate;
        }

        public void setVideoFileDir(String videoFileDir) {
            int len = videoFileDir.length() - 1;
            if ("/".equals(videoFileDir.substring(len))) {
                this.mVideoFileDir = videoFileDir.substring(0, len);
            } else {
                this.mVideoFileDir = videoFileDir;
            }
        }

        public void setVideoFileName(String videoFileName) {
            this.mVideoFileName = videoFileName;
        }

        public void setEglContext(EGLContext eglContext) {
            this.mEglContext = eglContext;
        }

        public void setInputTexId(int inputTexId) {
            this.mInputTexId = inputTexId;
        }
    }


}
