package com.wyroczen.alphacamera.stock;

import java.lang.reflect.Method;

public class ReflectUtils {
    private static boolean matches(Class<?>[] clsArr, Object[] objArr) {
        boolean z = true;
        if (clsArr == null || clsArr.length == 0) {
            if (!(objArr == null || objArr.length == 0)) {
                z = false;
            }
            return z;
        } else if (objArr == null || clsArr.length != objArr.length) {
            return false;
        } else {
            int i = 0;
            while (i < clsArr.length) {
                if (objArr[i] != null && !clsArr[i].isAssignableFrom(objArr[i].getClass())) {
                    return false;
                }
                i++;
            }
            return true;
        }
    }

    public static Method findMethod(Class<? extends Object> cls, String str, Object[] objArr) {
        for (Method method : cls.getDeclaredMethods()) {
            if (method.getName().equals(str) && matches(method.getParameterTypes(), objArr)) {
                return method;
            }
        }
        Class superclass = cls.getSuperclass();
        if (superclass != null) {
            return findMethod(superclass, str, objArr);
        }
        return null;
    }

    public static <T> T invoke(Object obj, String str, Object[] objArr) {
        try {
            Method findMethod = findMethod(obj.getClass(), str, objArr);
            findMethod.setAccessible(true);
            return (T) findMethod.invoke(obj, objArr);
        } catch (Exception e) {

            return null;
        }
    }

    public static <T> T invokeStatic(String str, String str2, Object[] objArr) {
        try {
            Method findMethod = findMethod(Class.forName(str), str2, objArr);
            findMethod.setAccessible(true);
            return (T) findMethod.invoke(null, objArr);
        } catch (Exception e) {

            return null;
        }
    }

}
