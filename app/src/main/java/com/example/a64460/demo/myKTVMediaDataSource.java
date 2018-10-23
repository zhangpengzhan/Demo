package com.example.a64460.demo;

import android.annotation.SuppressLint;
import android.media.MediaDataSource;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.*;
import java.nio.channels.*;

@SuppressLint("NewApi")
public class myKTVMediaDataSource extends MediaDataSource {
    private static final String TAG = "myKTVMediaDataSource";
    public static final String path = "file:///mnt/sdcard/home-2.mp4";
    private  String mUrl="";
    private RandomAccessFile mFile=null;
    private FileChannel mFC=null;
    private MappedByteBuffer mBuffer=null;
    private long mLength=0;
    public myKTVMediaDataSource(String path) throws FileNotFoundException {
        Log.d(TAG, "open=============:"+path);
        mUrl=path;
        final Uri uri = Uri.parse(path);
        final String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            path = uri.getPath();
        } else if (scheme != null) {
            // handle non-file sources

            //return;
        }

        final File file = new File(path);
        if (file.exists()) {
            mFile = new RandomAccessFile(file,"rw");
            try {
                mLength=mFile.length();
                Log.d(TAG, "song file length:"+mLength);
            } catch (IOException e) {
                Log.d(TAG, "fc map failed!");
            }

        } else {
            throw new FileNotFoundException();
        }
    }
    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {

        if(mFile!=null){
            mFile.seek(position);
            int n=mFile.read(buffer,offset,size);
            Log.d(TAG, "readAt:"+position+",file pointer"+mFile.getFilePointer()+",offset:"+offset+",size:"+size+",read len:"+n);
            return n;
        }
        return 0;
    }

    @Override
    public long getSize() throws IOException {
        Log.d(TAG, "getSize()!:"+mLength);
        return mLength;
    }

    @Override
    public void close() throws IOException {
        Log.d(TAG, "close()!");
        if(mFile!=null)
            mFile.close();
    }
}
