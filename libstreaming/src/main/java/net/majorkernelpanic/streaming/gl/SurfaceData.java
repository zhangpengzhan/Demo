package net.majorkernelpanic.streaming.gl;

import android.hardware.Camera;

import net.majorkernelpanic.streaming.callback.VideoStreamCallBack;
import net.majorkernelpanic.streaming.video.VideoQuality;

public interface SurfaceData {
    Camera getmCamera();
    VideoQuality getmQuality();
    VideoStreamCallBack getVideoStreamCallBack();
}
