package com.wyroczen.alphacamera.jni;

public class HookerLibJNI {
    static {
        System.loadLibrary("liberation-lib");
    }

    public native void doHook();

    public native void printHelloInLog();
}
