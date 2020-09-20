package com.wyroczen.alphacamera.jni;

public class NativeLibJNI {
    static {
        System.loadLibrary("native-lib");
    }

    private native void printWelcome();

    public native String getWelcome();
}
