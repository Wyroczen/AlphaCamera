package com.wyroczen.alphacamera.reflection.utils;

public class HashCodeHelpers {
    public static int hashCode(int... array) {
        if (array == null) {
            return 0;
        }
        /*
         *  Note that we use 31 here instead of 33 since it's preferred in Effective Java
         *  and used elsewhere in the runtime (e.g. Arrays#hashCode)
         *
         *  That being said 33 and 31 are nearly identical in terms of their usefulness
         *  according to http://svn.apache.org/repos/asf/apr/apr/trunk/tables/apr_hash.c
         */
        int h = 1;
        for (int x : array) {
            // Strength reduction; in case the compiler has illusions about divisions being faster
            h = ((h << 5) - h) ^ x; // (h * 31) XOR x
        }
        return h;
    }
}
