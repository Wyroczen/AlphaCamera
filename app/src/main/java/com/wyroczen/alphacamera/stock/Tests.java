package com.wyroczen.alphacamera.stock;

import android.view.Surface;

import java.util.Set;

public class Tests {
    public static void clearSurfaces(Set<Surface> set){
        for(Surface s : set){
            s.release();
        }
    }
}
