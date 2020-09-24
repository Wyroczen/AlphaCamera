package com.wyroczen.alphacamera.jni;

import android.util.Size;

public class NativeLibJNI {
    static {
        System.loadLibrary("native-lib");
    }

    private native void printWelcome();

    public native String getWelcome();

    public native boolean isSignatureCorrect();

    public native Size[] removeSize(Size[] sizes, int size);
}
