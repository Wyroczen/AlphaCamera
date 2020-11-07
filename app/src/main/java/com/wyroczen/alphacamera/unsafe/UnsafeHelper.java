package com.wyroczen.alphacamera.unsafe;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Environment;
import android.util.Log;

import com.ironz.unsafe.UnsafeAndroid;

import java.io.File;
import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

import static java.lang.invoke.MethodHandles.lookup;

public class UnsafeHelper {

    private UnsafeAndroid unsafeObject;
    private Activity activity;

    public UnsafeHelper(Activity activity) {
        stealUnsafe();
        this.activity = activity;
    }

    private static long normalize(int value) {
        if (value >= 0) return value;
        return (~0L >>> 32) & value;
    }

    private void stealUnsafe() {
        //Field f = Unsafe.class.getDeclaredField("theUnsafe");
        //Unsafe unsafe = (Unsafe) f.get(null);
        unsafeObject = new UnsafeAndroid();
    }

    public long sizeOf(Object o) {
        HashSet<Field> fields = new HashSet<Field>();
        Class c = o.getClass();
        while (c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if ((f.getModifiers() & Modifier.STATIC) == 0) {
                    fields.add(f);
                }
            }
            c = c.getSuperclass();
        }

        // get offset
        long maxSize = 0;
        for (Field f : fields) {
            long offset = unsafeObject.objectFieldOffset(f);
            if (offset > maxSize) {
                maxSize = offset;
            }
        }

        return ((maxSize / 8) + 1) * 8;   // padding
    }

    public Object shallowCopy(Object obj) {
        long size = sizeOf(obj);
        long start = toAddress(obj);
        long address = unsafeObject.allocateMemory(size);
        unsafeObject.copyMemory(start, address, size);
        return fromAddress(address);
    }

    private long toAddress(Object obj) {
        Object[] array = new Object[]{obj};
        long baseOffset = unsafeObject.arrayBaseOffset(Object[].class);
        return normalize(unsafeObject.getInt(array, baseOffset));
    }

    private Object fromAddress(long address) {
        Object[] array = new Object[]{null};
        long baseOffset = unsafeObject.arrayBaseOffset(Object[].class);
        unsafeObject.putLong(array, baseOffset, address);
        return array[0];
    }

    private static byte[] getClassContent() throws Exception {
        File f = new File("/home/mishadoff/tmp/A.class");
        FileInputStream input = new FileInputStream(f);
        byte[] content = new byte[(int) f.length()];
        input.read(content);
        input.close();
        return content;
    }


    public Object loadClassFromInstalledApp(String packageName, String className) {
        Object plugin = null;
        try {
            PackageManager packageManager = activity.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            DexFile df = new DexFile(appInfo.sourceDir);
            ClassLoader cl = activity.getClassLoader();
            Class classToInvestigate = df.loadClass(className, cl);
            plugin = classToInvestigate.newInstance();
        } catch (Exception e) {
            System.out.println("EXCEPTION");
        }
        finally{
            Log.i("AlphaCamera-Unsafe", "Class loaded!");
            return plugin;
        }
    }

    private Signature getAppSignature(){
        Signature[] sigs = new Signature[0];
        try {
            sigs = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return sigs[0];
    }

    public Object loadClassFromFile() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String className = "com.wyroczen.wyroczenhelper.WyroczenHelper";
        String apkFile = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/WyroczenHelper.apk";
        Log.i("AlphaCamera-Unsafe", "Apk: " + apkFile);
        final File optimizedDexOutputPath = activity.getDir("outdex", 0);
        DexClassLoader dLoader = new DexClassLoader(apkFile,optimizedDexOutputPath.getAbsolutePath(),
                null,ClassLoader.getSystemClassLoader().getParent());

        Class<?> loadedClass = null;
        try {
            loadedClass = dLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String hello = (String) loadedClass.getDeclaredMethod("printHello").invoke(loadedClass.newInstance());
        Log.i("AlphaCamera-Unsafe", "Class loaded!" + hello);

        return loadedClass.newInstance();
    }

    public void loadClass() {
        // Get the plug-in power pushed forward to SDCard
        //String apkPath = Environment.getExternalStorageDirectory() + File.separator + "/Download/WyroczenHelper.apk";
        String apkPath = activity.getExternalFilesDir(null).toString() + "/Helpers";
        // Optimized dex storage path
        Log.i("AlphaCamera-Unsafe", "Apk: " + apkPath);
        String dexOutput = activity.getCacheDir() + File.separator + "DEX";
        File file = new File(dexOutput);
        if (!file.exists()) file.mkdirs();
        DexClassLoader dexClassLoader = new DexClassLoader(apkPath, dexOutput, null, activity.getClassLoader());
        try {
            // Loading APK_HELLO_CLASS_PATH class from the optimized dex file
            Class clazz = dexClassLoader.loadClass("com.wyroczen.wyroczenhelper.WyroczenHelper");
            Log.i("AlphaCamera-Unsafe", "Class loaded!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
