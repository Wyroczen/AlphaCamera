package com.wyroczen.alphacamera.reflection;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ChangeCharacteristics {
    public static CameraCharacteristics characteristics;
    public static ChangeCharacteristics instance;
    public ChangeCharacteristics(){
        instance = this;
    }
    public void setCharacteristics(CameraCharacteristics characteristicss){
        characteristics = characteristicss;
    }
    public static Object get(StreamConfigurationMap map, String name){
        Object curobj = null;
        try {
            Field ObjectField = map.getClass().getDeclaredField(name);
            ObjectField.setAccessible(true);
            curobj = ObjectField.get(map);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return curobj;
    }
    public static StreamConfiguration getStream(StreamConfigurationMap map, String name, int ind){
        try {
            Field selected = map.getClass().getDeclaredField(name);
            selected.setAccessible(true);
            Object arr = selected.get(map);
            return (StreamConfiguration) Array.get(arr,ind);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void setStream(StreamConfigurationMap map, String name, int ind,StreamConfiguration streamConfiguration){
        try {
            Field selected = map.getClass().getDeclaredField(name);
            selected.setAccessible(true);
            Object arr = selected.get(map);
            Array.set(arr,ind,streamConfiguration);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    public static <T>  void relloc(CameraCharacteristics.Key<T> keyfrom, CameraCharacteristics.Key<T> keyto){
        try {
            Field CameraMetadataNativeField = characteristics.getClass().getDeclaredField("mProperties");
            CameraMetadataNativeField.setAccessible(true);
            Object CameraMetadataNative = CameraMetadataNativeField.get(characteristics);//Ur camera Characteristics
            Method set =CameraMetadataNative.getClass().getDeclaredMethod("set",CameraCharacteristics.Key.class, Object.class);
            set.setAccessible(true);
            Object value = characteristics.get(keyfrom);
            set.invoke(CameraMetadataNative, keyto, value);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    public static <T> void set(CameraCharacteristics.Key<T> key, T value){
        try {
            Field CameraMetadataNativeField = characteristics.getClass().getDeclaredField("mProperties");
            CameraMetadataNativeField.setAccessible(true);
            Object CameraMetadataNative = CameraMetadataNativeField.get(characteristics);//Ur camera Characteristics
            Method set =CameraMetadataNative.getClass().getDeclaredMethod("set",CameraCharacteristics.Key.class, Object.class);
            set.setAccessible(true);
            set.invoke(CameraMetadataNative, key, value);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
