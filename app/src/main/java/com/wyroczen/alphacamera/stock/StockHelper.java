package com.wyroczen.alphacamera.stock;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Log;
import android.util.Size;

import com.wyroczen.alphacamera.reflection.ReflectionHelper;

public class StockHelper {
    public static void copyFpcDataFromCaptureResultToRequest(CaptureResult captureResult, CaptureRequest.Builder  builder) {
        byte[] byteArray = (byte[]) captureResult.get(ReflectionHelper.DISTORTION_FPC_DATA); //VendorTagHelper.getValueQuietly(captureResult, CaptureResultVendorTags.DISTORTION_FPC_DATA);
        for(byte b : byteArray){
            Log.i("AlphaCamera - distortion:", "Byte: " + String.valueOf(b));
        }
        if (byteArray != null && byteArray.length / 8 == 23) {
            builder.set(ReflectionHelper.CONTROL_DISTORTION_FPC_DATA, byteArray); //VendorTagHelper.setValueQuietly(builder, CaptureRequestVendorTags.CONTROL_DISTORTION_FPC_DATA, bArr);
            Log.i("AlphaCamera Stock", "Distortion data applied");
        }
    }

    public static Size[] removeSize(Size[] sizes){
        for(int i = 0; i < sizes.length; i++){
            if(sizes[i].getHeight() == 6936){
                sizes[i] = new Size(4640, 3472);
            }
        }
        return sizes;
    }

}
