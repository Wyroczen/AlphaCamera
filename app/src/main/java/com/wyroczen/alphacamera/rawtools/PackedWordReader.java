package com.wyroczen.alphacamera.rawtools;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import com.wyroczen.alphacamera.jni.NativeLibJNI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class PackedWordReader {

    private final String TAG = "AlphaCamera-PWR";
    private Context context;

    public PackedWordReader(Context context) {
        this.context = context;
    }

    public ByteBuffer doInNative() {
        String packedDataPath = context.getExternalFilesDir(null).toString() + "/Packed/64.packed_word";
        File packedData = new File(packedDataPath);
        ByteBuffer byteBuffer = null;

        byte[] packedBytes = new byte[0];
        try {
            packedBytes = Files.readAllBytes(packedData.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        NativeLibJNI nativeJni = new NativeLibJNI();
        String[] bitsInStrings = new String[packedBytes.length / 5];
        //bitsInStrings = nativeJni.processBytes(packedBytes);

        Log.i(TAG, "We've got data from native!");

//        byte[] newBytesArray = new byte[bitsInStrings.length * 4];
//        for(int i = 0; i < bitsInStrings.length; i++){
//            String base2_10_1 = bitsInStrings[i].substring(0,8) + bitsInStrings[i].substring(38,40);
//            Log.i(TAG, " Value: " + base2_10_1);
//            String base2_10_2 = bitsInStrings[i].substring(8,16) + bitsInStrings[i].substring(36,38);
//            String base2_10_3 = bitsInStrings[i].substring(16,24) + bitsInStrings[i].substring(34,36);
//            String base2_10_4 = bitsInStrings[i].substring(24,32) + bitsInStrings[i].substring(32,34);
//            int decimal1 = Integer.parseInt(base2_10_1,2) * 64;
//            int decimal2 = Integer.parseInt(base2_10_2,2) * 64;
//            int decimal3 = Integer.parseInt(base2_10_3,2) * 64;
//            int decimal4 = Integer.parseInt(base2_10_4,2) * 64;
//            Log.i(TAG, " Value: " + decimal1);
//            byte byte1 = (byte) decimal1;
//            byte byte2 = (byte) decimal2;
//            byte byte3 = (byte) decimal3;
//            byte byte4 = (byte) decimal4;
//            newBytesArray[i] = byte1;
//            newBytesArray[i+1] = byte2;
//            newBytesArray[i+2] = byte3;
//            newBytesArray[i+3] = byte4;
//        }
//        byteBuffer = ByteBuffer.wrap(newBytesArray);
        return byteBuffer;

    }

    public ByteBuffer getByteBuffer() {
        String packedDataPath = context.getExternalFilesDir(null).toString() + "/Packed/64_2.packed_word";
        File packedData = new File(packedDataPath);
        byte[] packedBytes = null;
        try {
            packedBytes = Files.readAllBytes(packedData.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream output16 = new ByteArrayOutputStream();

        for(int i = 0; i < packedBytes.length; i += 5) {
            String[] bitsInStrings = new String[1];
            StringBuilder sb = new StringBuilder();
            String bs = String.format("%8s", Integer.toBinaryString(packedBytes[i] & 0xFF)).replace(' ', '0');
            String bs1 = String.format("%8s", Integer.toBinaryString(packedBytes[i+1] & 0xFF)).replace(' ', '0');
            String bs2 = String.format("%8s", Integer.toBinaryString(packedBytes[i+2] & 0xFF)).replace(' ', '0');
            String bs3 = String.format("%8s", Integer.toBinaryString(packedBytes[i+3] & 0xFF)).replace(' ', '0');
            String bs4 = String.format("%8s", Integer.toBinaryString(packedBytes[i+4] & 0xFF)).replace(' ', '0');
            sb.append(bs);
            sb.append(bs1);
            sb.append(bs2);
            sb.append(bs3);
            sb.append(bs4);
            bitsInStrings[0] = sb.toString();
            String base2_10_1 = bitsInStrings[0].substring(0, 8) + bitsInStrings[0].substring(38, 40);
            String base2_10_2 = bitsInStrings[0].substring(8, 16) + bitsInStrings[0].substring(36, 38);
            String base2_10_3 = bitsInStrings[0].substring(16, 24) + bitsInStrings[0].substring(34, 36);
            String base2_10_4 = bitsInStrings[0].substring(24, 32) + bitsInStrings[0].substring(32, 34);
            int decimal1 = Integer.parseInt(base2_10_1, 2) * 64;
            int decimal2 = Integer.parseInt(base2_10_2, 2) * 64;
            int decimal3 = Integer.parseInt(base2_10_3, 2) * 64;
            int decimal4 = Integer.parseInt(base2_10_4, 2) * 64;
            output16.write((byte) (decimal1 >>> 8));
            output16.write((byte) decimal1);
            output16.write((byte) (decimal2 >>> 8));
            output16.write((byte) decimal2);
            output16.write((byte) (decimal3 >>> 8));
            output16.write((byte) decimal3);
            output16.write((byte) (decimal4 >>> 8));
            output16.write((byte) decimal4);
        }

        byte[] packedBytes16 = output16.toByteArray();
        ByteBuffer byteBuffer = ByteBuffer.wrap(packedBytes16);
        return byteBuffer;
    }

    //private static convertYUVtoJPEG(){
    //    Image =
    //    data = NV21toJPEG(YUV420toNV21(image), image.getWidth(), image.getHeight(), 100);
    //}

    public static byte[] NV21toJPEG(byte[] nv21, int width, int height, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), quality, out);
        return out.toByteArray();
    }

    public static byte[] YUV420toNV21(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }

            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

    public ByteBuffer getByteBufferOld() {
        String packedDataPath = context.getExternalFilesDir(null).toString() + "/Packed/64.packed_word";
        File packedData = new File(packedDataPath);
        ByteBuffer byteBuffer = null;
        try {
            byte[] packedBytes = Files.readAllBytes(packedData.toPath());

            String[] bitsInStrings = new String[packedBytes.length / 5];
            int j = 0;
            for (int i = 0; i < packedBytes.length; i = i + 5) {
                StringBuilder sb = new StringBuilder();
                String bs = String.format("%8s", Integer.toBinaryString(packedBytes[i] & 0xFF)).replace(' ', '0');
                String bs1 = String.format("%8s", Integer.toBinaryString(packedBytes[i + 1] & 0xFF)).replace(' ', '0');
                String bs2 = String.format("%8s", Integer.toBinaryString(packedBytes[i + 2] & 0xFF)).replace(' ', '0');
                String bs3 = String.format("%8s", Integer.toBinaryString(packedBytes[i + 3] & 0xFF)).replace(' ', '0');
                String bs4 = String.format("%8s", Integer.toBinaryString(packedBytes[i + 4] & 0xFF)).replace(' ', '0');
                sb.append(bs);
                sb.append(bs1);
                sb.append(bs2);
                sb.append(bs3);
                sb.append(bs4);
                bitsInStrings[j] = sb.toString();
                j++;
            }

            byte[] newBytesArray = new byte[bitsInStrings.length * 4];
            for (int i = 0; i < bitsInStrings.length; i++) {
                String base2_10_1 = bitsInStrings[i].substring(0, 8) + bitsInStrings[i].substring(38, 40);
                Log.i(TAG, " Value: " + base2_10_1);
                String base2_10_2 = bitsInStrings[i].substring(8, 16) + bitsInStrings[i].substring(36, 38);
                String base2_10_3 = bitsInStrings[i].substring(16, 24) + bitsInStrings[i].substring(34, 36);
                String base2_10_4 = bitsInStrings[i].substring(24, 32) + bitsInStrings[i].substring(32, 34);
                int decimal1 = Integer.parseInt(base2_10_1, 2) * 64;
                int decimal2 = Integer.parseInt(base2_10_2, 2) * 64;
                int decimal3 = Integer.parseInt(base2_10_3, 2) * 64;
                int decimal4 = Integer.parseInt(base2_10_4, 2) * 64;
                Log.i(TAG, " Value: " + decimal1);
                byte byte1 = (byte) decimal1;
                byte byte2 = (byte) decimal2;
                byte byte3 = (byte) decimal3;
                byte byte4 = (byte) decimal4;
                newBytesArray[i] = byte1;
                newBytesArray[i + 1] = byte2;
                newBytesArray[i + 2] = byte3;
                newBytesArray[i + 3] = byte4;
            }
            byteBuffer = ByteBuffer.wrap(newBytesArray);
//            byteBuffer = ByteBuffer.wrap(ByteBuffer.allocate(packedBytes.length + packedBytes2.length)
//                    .put(packedBytes)
//                    .put(packedBytes2)
//                    .array());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteBuffer;
    }

    /**
     * Saves RAW {@link Image} into the specified {@link File}.
     */
    public static class PackedImageSaver implements Runnable {

        private final File mFile;
        private final CameraCharacteristics mCameraCharacteristics;
        private final CaptureResult mCaptureResult;
        private final ByteBuffer mByteBuffer;

        public PackedImageSaver(File file, CameraCharacteristics cameraCharacteristics, CaptureResult captureResult, ByteBuffer byteBuffer) {
            mFile = file;
            mCameraCharacteristics = cameraCharacteristics;
            mCaptureResult = captureResult;
            mByteBuffer = byteBuffer;
        }


        @Override
        public void run() {

            DngCreator dngCreator = new DngCreator(mCameraCharacteristics, mCaptureResult);
            FileOutputStream rawFileOutputStream = null;
            try {
                long capacity = mByteBuffer.capacity();
                Log.i("AlphaCamera-PWR", "Capacity: " + capacity);
                rawFileOutputStream = new FileOutputStream(mFile);
                dngCreator.writeByteBuffer(rawFileOutputStream, new Size(9280, 6944), mByteBuffer, 0);
//                try {
//                    ReflectionHelper.customWriteByteBuffer.invoke(dngCreator,4640, 3472, mByteBuffer, rawFileOutputStream, 2, 9280, 0);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
