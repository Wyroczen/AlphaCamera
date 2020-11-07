package com.wyroczen.alphacamera.reflection;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import com.wyroczen.alphacamera.rawtools.PackedWordReader;

public class ImageReader2 implements AutoCloseable {

    private final String TAG = "AlphaCamera-ImageReader2";
    private int mWidth;
    private int mHeight;
    private int mFormat;
    private int mMaxImages;
    private Boolean overridden = false;
    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener onImageAvailableListener;

    public ImageReader2(int width, int height, int format, int maxImages) {
        this.mWidth = width;
        this.mHeight = height;
        this.mFormat = format;
        this.mMaxImages = maxImages;

        if(format == ImageFormat.RAW_SENSOR){
            format = ImageFormat.YUV_420_888;
            this.overridden = true;
        }
        this.imageReader = ImageReader.newInstance(width, height, format, maxImages);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getImageFormat() {
        return mFormat;
    }

    public int getMaxImages() {
        return mMaxImages;
    }

    public Surface getSurface() {
        return imageReader.getSurface();
    }

    public Image acquireLatestImage() {
        return imageReader.acquireLatestImage();
    }

    public Image acquireNextImage() {
        Log.i(TAG," Acquiring image");
        Image image =  imageReader.acquireNextImage();
        if(this.overridden){
            byte[] bytes = PackedWordReader.NV21toJPEG(PackedWordReader.YUV420toNV21(image), image.getWidth(), image.getHeight(), 100);
        }
        return image;
    }

    public void setOnImageAvailableListener(ImageReader.OnImageAvailableListener listener, Handler handler){
        onImageAvailableListener = listener;
        imageReader.setOnImageAvailableListener(listener,handler);
    }

    public void discardFreeBuffers(){
        imageReader.discardFreeBuffers();
    }

    //public interface OnImageAvailableListener {
    //    void onImageAvailable(ImageReader2 reader);
    //}

    @Override
    public void close() {
        imageReader.close();
    }
}
