package net.majorkernelpanic.streaming.callback;

import android.hardware.Camera;
import android.media.MediaCodec;
import android.util.Log;

import net.majorkernelpanic.streaming.hw.NV21Convertor;
import net.majorkernelpanic.streaming.video.VideoStream;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VideoStreamCallBack implements Camera.PreviewCallback {
    private final String TAG = VideoStreamCallBack.class.getSimpleName();
    MediaCodec mMediaCodec;
    ByteBuffer[] inputBuffers;
    long now, oldnow;
    NV21Convertor convertor;
    Camera mCamera;
    onEnterFrameCallBack onEnterFrameCallBack;
    ConcurrentLinkedQueue<VideoStreamCallBack.onEnterFrameCallBack> onEnterFrameCallBacks;

    public VideoStreamCallBack(Camera camera, NV21Convertor convertor, MediaCodec mMediaCodec) {
        this.mMediaCodec = mMediaCodec;
        this.convertor = convertor;
        this.mCamera = camera;
        now = System.nanoTime() / 1000;
        oldnow = now;
        onEnterFrameCallBacks = new ConcurrentLinkedQueue<>();

    }

    public VideoStreamCallBack(Camera mCamera) {
        this.mCamera = mCamera;
        onEnterFrameCallBacks = new ConcurrentLinkedQueue<>();
    }

    public VideoStreamCallBack restartData() {
        now = System.nanoTime() / 1000;
        oldnow = now;

        return this;
    }

    public VideoStreamCallBack setmMediaCodec(MediaCodec mMediaCodec) {
        this.mMediaCodec = mMediaCodec;
        inputBuffers = mMediaCodec.getInputBuffers();
        //Log.d(TAG, "onPreviewFrame: ===============MM2::"+mMediaCodec);
        return this;
    }

    public VideoStreamCallBack setConvertor(NV21Convertor convertor) {
        this.convertor = convertor;
        return this;
    }

    public VideoStreamCallBack setmCamera(Camera mCamera) {
        this.mCamera = mCamera;
        return this;
    }

    public MediaCodec getmMediaCodec() {
        return mMediaCodec;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        oldnow = now;
        now = System.nanoTime() / 1000;
        //Log.d(TAG, "onPreviewFrame: ==="+data.length);
        try {
            //Log.d(TAG, "onPreviewFrame: ===============MM1::"+mMediaCodec);
            /*if (mMediaCodec != null) {
               // Log.d(TAG, "onPreviewFrame: ===============MM0::"+mMediaCodec);
                int bufferIndex = mMediaCodec.dequeueInputBuffer(500000);
                if (bufferIndex >= 0) {
                    inputBuffers[bufferIndex].clear();
                    convertor.convert(data, inputBuffers[bufferIndex]);
                    mMediaCodec.queueInputBuffer(bufferIndex, 0, inputBuffers[bufferIndex].position(), now, 0);
                } else {
                    Log.e(TAG, "No buffer available !");

                }
            }*/
            if (getOnEnterFrameCallBacks().size()>0){
                for (VideoStreamCallBack.onEnterFrameCallBack onEnterFrameCallBack:getOnEnterFrameCallBacks())
                    onEnterFrameCallBack.onDataBack(data,camera,now);
            }
        } finally {
            if (mCamera != null)
                mCamera.addCallbackBuffer(data);
        }

       // Log.d(TAG, "onPreviewFrame: ==========MM::"+mMediaCodec+"|"+mCamera);

    }

    public  void removeOnEnterFrameCallBack(VideoStreamCallBack.onEnterFrameCallBack onEnterFrameCallBack) {
        getOnEnterFrameCallBacks().remove(onEnterFrameCallBack);
    }

    public  synchronized ConcurrentLinkedQueue<VideoStreamCallBack.onEnterFrameCallBack> getOnEnterFrameCallBacks() {
        return onEnterFrameCallBacks;
    }

    public  void setOnEnterFrameCallBack(VideoStreamCallBack.onEnterFrameCallBack onEnterFrameCallBack) {
       // this.onEnterFrameCallBack = onEnterFrameCallBack;
        getOnEnterFrameCallBacks().add(onEnterFrameCallBack);
    }

    public interface onEnterFrameCallBack{
        void onDataBack(byte[] data, Camera camera,long time);
    }
}
