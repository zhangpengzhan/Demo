/*
 * Copyright (C) 2011-2014 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.majorkernelpanic.streaming.gl;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import net.majorkernelpanic.streaming.callback.VideoStreamCallBack;
import net.majorkernelpanic.streaming.exceptions.CameraInUseException;
import net.majorkernelpanic.streaming.exceptions.ConfNotSupportedException;
import net.majorkernelpanic.streaming.exceptions.InvalidSurfaceException;
import net.majorkernelpanic.streaming.hw.EncoderDebugger;
import net.majorkernelpanic.streaming.hw.NV21Convertor;
import net.majorkernelpanic.streaming.video.VideoQuality;

import static net.majorkernelpanic.streaming.MediaStream.MODE_MEDIACODEC_API;
import static net.majorkernelpanic.streaming.MediaStream.MODE_MEDIACODEC_API_2;

public class SurfaceView extends android.view.SurfaceView implements Runnable, OnFrameAvailableListener, SurfaceHolder.Callback,SurfaceData {

	public final static String TAG = "GLSurfaceView";

	private Thread mThread = null;
	private boolean mFrameAvailable = false; 
	private boolean mRunning = true;

	private SurfaceManager mViewSurfaceManager = null;
	private SurfaceManager mCodecSurfaceManager = null;
	private TextureManager mTextureManager = null;
	
	private Semaphore mLock = new Semaphore(0);
	private Object mSyncObject = new Object();



	public VideoQuality mRequestedQuality = VideoQuality.DEFAULT_VIDEO_QUALITY.clone();
	public VideoQuality mQuality = mRequestedQuality.clone();
	public SurfaceView mSurfaceView = this;
	public SharedPreferences mSettings = null;
	public int mVideoEncoder, mCameraId = 0;
	public int mRequestedOrientation = 0, mOrientation = 0;
	public Camera mCamera;
	public Thread mCameraThread;
	public Looper mCameraLooper;

	public boolean mCameraOpenedManually = true;
	public boolean mFlashEnabled = false;
	public boolean mSurfaceReady = false;
	public boolean mUnlocked = false;
	public boolean mPreviewStarted = false;

	public int mCameraImageFormat;
	private byte mMode = MODE_MEDIACODEC_API;
	private boolean mStreaming;

	private VideoStreamCallBack videoStreamCallBack;

	public SurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);

	}	

	public SurfaceTexture getSurfaceTexture() {
		return mTextureManager.getSurfaceTexture();
	}

	public void addMediaCodecSurface(Surface surface) {
		synchronized (mSyncObject) {
			mCodecSurfaceManager = new SurfaceManager(surface,mViewSurfaceManager);			
		}
	}
	
	public void removeMediaCodecSurface() {
		synchronized (mSyncObject) {
			if (mCodecSurfaceManager != null) {
				mCodecSurfaceManager.release();
				mCodecSurfaceManager = null;
			}
		}
	}
	
	public void startGLThread() {
		Log.d(TAG,"Thread started.");
		if (mTextureManager == null) {
			mTextureManager = new TextureManager();
		}
		if (mTextureManager.getSurfaceTexture() == null) {
			mThread = new Thread(SurfaceView.this);
			mRunning = true;
			mThread.start();
			mLock.acquireUninterruptibly();
		}
	}
	
	@Override
	public void run() {

		mViewSurfaceManager = new SurfaceManager(getHolder().getSurface());
		mViewSurfaceManager.makeCurrent();
		mTextureManager.createTexture().setOnFrameAvailableListener(this);

		mLock.release();

		try {
			long ts = 0, oldts = 0;
			while (mRunning) {
				synchronized (mSyncObject) {
					mSyncObject.wait(2500);
					if (mFrameAvailable) {
						mFrameAvailable = false;

						mViewSurfaceManager.makeCurrent();
						mTextureManager.updateFrame();
						mTextureManager.drawFrame();
						mViewSurfaceManager.swapBuffer();
						
						if (mCodecSurfaceManager != null) {
							mCodecSurfaceManager.makeCurrent();
							mTextureManager.drawFrame();
							oldts = ts;
							ts = mTextureManager.getSurfaceTexture().getTimestamp();
							//Log.d(TAG,"FPS: "+(1000000000/(ts-oldts)));
							mCodecSurfaceManager.setPresentationTime(ts);
							mCodecSurfaceManager.swapBuffer();
						}
						
					} else {
						Log.e(TAG,"No frame received !");
					}
				}
			}
		} catch (InterruptedException ignore) {
		} finally {
			mViewSurfaceManager.release();
			mTextureManager.release();
		}
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		synchronized (mSyncObject) {
			mFrameAvailable = true;
			mSyncObject.notifyAll();	
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mThread != null) {
			mThread.interrupt();
		}
		mRunning = false;
	}



	private void openCamera() throws RuntimeException {
		Log.d(TAG, "openCamera: ===========");
		final Semaphore lock = new Semaphore(0);
		final RuntimeException[] exception = new RuntimeException[1];
		mCameraThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				mCameraLooper = Looper.myLooper();
				try {
					mCamera = Camera.open(mCameraId);
				} catch (RuntimeException e) {
					exception[0] = e;
				} finally {
					lock.release();
					Looper.loop();
				}
			}
		});
		mCameraThread.start();
		lock.acquireUninterruptibly();
		if (exception[0] != null) throw new CameraInUseException(exception[0].getMessage());
	}
	public synchronized void createCamera() throws RuntimeException {
		Log.d(TAG, "createCamera: ============");
		/*if (mSurfaceView == null)
			throw new InvalidSurfaceException("Invalid surface !");
		if (mSurfaceView.getHolder() == null || !mSurfaceReady)
			throw new InvalidSurfaceException("Invalid surface !");*/

		if (mCamera == null) {
			openCamera();
			mUnlocked = false;
			mCamera.setErrorCallback(new Camera.ErrorCallback() {
				@Override
				public void onError(int error, Camera camera) {
					// On some phones when trying to use the camera facing front the media server will die
					// Whether or not this callback may be called really depends on the phone
					if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
						// In this case the application must release the camera and instantiate a new one
						Log.e(TAG,"Media server died !");
						// We don't know in what thread we are so stop needs to be synchronized
						mCameraOpenedManually = false;
						stop();
					} else {
						Log.e(TAG,"Error unknown with the camera: "+error);
					}
				}
			});

			try {

				// If the phone has a flash, we turn it on/off according to mFlashEnabled
				// setRecordingHint(true) is a very nice optimisation if you plane to only use the Camera for recording
				Camera.Parameters parameters = mCamera.getParameters();
				if (parameters.getFlashMode()!=null) {
					parameters.setFlashMode(mFlashEnabled?Camera.Parameters.FLASH_MODE_TORCH:Camera.Parameters.FLASH_MODE_OFF);
				}
				parameters.setRecordingHint(true);
				mCamera.setParameters(parameters);
				mCamera.setDisplayOrientation(mOrientation);
				try {
					if (mMode == MODE_MEDIACODEC_API_2) {
						Log.d(TAG, "createCamera: ========MODE_MEDIACODEC_API_2");
						mSurfaceView.startGLThread();
						mCamera.setPreviewTexture(mSurfaceView.getSurfaceTexture());
					} else {
						Log.d(TAG, "createCamera: ===========MODE_MEDIACODEC_API_1");
						mCamera.setPreviewDisplay(mSurfaceView.getHolder());
					}
				} catch (IOException e) {
					throw new InvalidSurfaceException("Invalid surface !");
				}

			} catch (RuntimeException e) {
				destroyCamera();
				throw e;
			}

		}
	}

	public Camera getmCamera() {
		return mCamera;
	}

	public synchronized void destroyCamera() {
		Log.d(TAG, "destroyCamera: =========");
		if (mCamera != null) {
			if (mStreaming) stop();
			lockCamera();
			mCamera.stopPreview();
			try {
				mCamera.release();
			} catch (Exception e) {
				Log.e(TAG,e.getMessage()!=null?e.getMessage():"unknown error");
			}
			mCamera = null;
			mCameraLooper.quit();
			mUnlocked = false;
			mPreviewStarted = false;
		}
	}

	public VideoQuality getmQuality() {
		return mQuality;
	}

	public synchronized void updateCamera() throws RuntimeException {
		Log.d(TAG, "updateCamera: ==============");
		if (mPreviewStarted) {
			mPreviewStarted = false;
			mCamera.stopPreview();
		}

		Camera.Parameters parameters = mCamera.getParameters();
		mQuality = VideoQuality.determineClosestSupportedResolution(parameters, mQuality);
		int[] max = VideoQuality.determineMaximumSupportedFramerate(parameters);
		parameters.setPreviewFormat( ImageFormat.NV21);
		parameters.setPreviewSize(mQuality.resX, mQuality.resY);
		parameters.setPreviewFpsRange(max[0], max[1]);

		try {
			mCamera.setParameters(parameters);
			mCamera.setDisplayOrientation(mOrientation);
			mCamera.startPreview();
			mPreviewStarted = true;
		} catch (RuntimeException e) {
			destroyCamera();
			throw e;
		}
	}

	public void lockCamera() {
		if (mUnlocked) {
			Log.d(TAG,"Locking camera=====");
			try {
				mCamera.reconnect();
			} catch (Exception e) {
				Log.e(TAG,e.getMessage());
			}
			mUnlocked = false;
		}
	}

	public void unlockCamera() {
		if (!mUnlocked) {
			Log.d(TAG,"Unlocking camera==========");
			try {
				mCamera.unlock();
			} catch (Exception e) {
				Log.e(TAG,e.getMessage());
			}
			mUnlocked = true;
		}
	}
	/** Stops the stream. */
	public synchronized void stop() {
		Log.d(TAG, "stop: ===========");
		if (mCamera != null) {
			if (mMode == MODE_MEDIACODEC_API) {
				mCamera.setPreviewCallbackWithBuffer(null);
			}
			if (mMode == MODE_MEDIACODEC_API_2) {
				((SurfaceView)mSurfaceView).removeMediaCodecSurface();
			}
			// We need to restart the preview
			if (!mCameraOpenedManually) {
				destroyCamera();
			} else {
				try {
					startPreview();
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public synchronized void startPreview()
			throws CameraInUseException,
			InvalidSurfaceException,
			ConfNotSupportedException,
			RuntimeException {
		Log.d(TAG, "startPreview: =================");
		mCameraOpenedManually = true;
		if (!mPreviewStarted) {
			createCamera();
			updateCamera();
			try {
				mCamera.startPreview();
				EncoderDebugger debugger = EncoderDebugger.debug(PreferenceManager.getDefaultSharedPreferences(getContext()), mQuality.resX, mQuality.resY);
				final NV21Convertor convertor = debugger.getNV21Convertor();

				for (int i=0;i<10;i++) mCamera.addCallbackBuffer(new byte[convertor.getBufferSize()]);
				videoStreamCallBack = new VideoStreamCallBack(mCamera);
				mCamera.setPreviewCallbackWithBuffer(videoStreamCallBack);
				mPreviewStarted = true;
			} catch (RuntimeException e) {
				destroyCamera();
				throw e;
			}
		}
	}



	public VideoStreamCallBack getVideoStreamCallBack() {
		return videoStreamCallBack;
	}

	/**
	 * Stops the preview.
	 */
	public synchronized void stopPreview() {
		mCameraOpenedManually = false;
		stop();
	}
}
