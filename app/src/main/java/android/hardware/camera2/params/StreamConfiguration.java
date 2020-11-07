package com.wyroczen.alphacamera.reflection;

import android.util.Size;

import com.wyroczen.alphacamera.reflection.utils.HashCodeHelpers;


public class StreamConfiguration {

    protected int mFormat;
    protected int mWidth;
    protected int mHeight;
    protected boolean mInput;

    public StreamConfiguration(
            final int format, final int width, final int height, final boolean input) {
        mFormat = format;
        mWidth = width;
        mHeight = height;
        mInput = input;
    }

    public final int getFormat() {
        return mFormat;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Size getSize() {
        return new Size(mWidth, mHeight);
    }

    public boolean isInput() {
        return mInput;
    }

    public boolean isOutput() {
        return !mInput;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof StreamConfiguration) {
            final StreamConfiguration other = (StreamConfiguration) obj;
            return mFormat == other.mFormat &&
                    mWidth == other.mWidth &&
                    mHeight == other.mHeight &&
                    mInput == other.mInput;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCodeHelpers.hashCode(mFormat, mWidth, mHeight, mInput ? 1 : 0);
    }

}
