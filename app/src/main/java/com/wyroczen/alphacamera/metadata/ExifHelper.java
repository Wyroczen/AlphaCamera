package com.wyroczen.alphacamera.metadata;

import androidx.exifinterface.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.IOException;


public class ExifHelper {

    private static final String TAG = "AlphaCamera-Exif";

    private static StringBuilder sb = new StringBuilder(20);

    public static String[] getMetadata(File file) throws IOException {
        ExifInterface exif = new ExifInterface(file.getCanonicalPath());
        Log.i(TAG, "Path for getting exif data" + file.getCanonicalPath());
        String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        String imageDesc = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        //OVERRIDE LATITUDE AND LONGITUDE FROM IMAGE DESC //TODO FIX THIS
        String[] imageDescArray = imageDesc.split(" ");
        Log.i(TAG, "Readed exif data:" + longitude + longitudeRef + " " + latitude + latitudeRef);
        return new String[]{imageDescArray[1],imageDescArray[2], imageDescArray[0]};
    }

    public static void saveMetaData(File file, double latitude, double longitude) throws IOException {
        Log.i(TAG,"Metadata Save" );
        ExifInterface exif = new ExifInterface(file.getCanonicalPath());
        Log.i(TAG," File for exif: "+file.getAbsolutePath());
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convert(latitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRef(latitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convert(longitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeRef(longitude));
        exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION,  file.getName() + " " + latitude + " " + longitude);
        exif.saveAttributes();
        Log.i(TAG, "" + latitude + "," + longitude);
        Log.i(TAG, "" + convert(latitude) + "," + longitudeRef(longitude));
        Log.i(TAG, "" + latitudeRef(latitude) + "," + longitudeRef(longitude));
    }

    /**
     * returns ref for latitude which is S or N.
     * @param latitude
     * @return S or N
     */
    public static String latitudeRef(double latitude) {
        return latitude<0.0d?"S":"N";
    }


    public static String longitudeRef(double longitude) {
        return longitude<0.0d?"W":"E";
    }

    /**
     * convert latitude into DMS (degree minute second) format. For instance<br/>
     * -79.948862 becomes<br/>
     *  79/1,56/1,55903/1000<br/>
     * It works for latitude and longitude<br/>
     * @param latitude could be longitude.
     * @return
     */
    synchronized public static String convert(double latitude) {
        latitude=Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude*1000.0d);
        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000");
        Log.i(TAG," After conversion: "+sb.toString());
        return sb.toString();
    }

    synchronized public static String convertDMStoDegree(String dms) {
        String[] values = dms.split(",");
        String degree = values[0].split("/")[0];
        String minute = values[1].split("/")[0];
        String second = values[2].split("/")[0];
        double ddegree = Double.parseDouble(degree);
        double dsecond = Double.parseDouble(second)/1000;
        double dminute = Double.parseDouble(minute)/60;
        Log.i(TAG, "Dsecond: " + dsecond + " Dminute: " + dminute);
        //double dminute = Double.parseDouble(minute)
        double degrees = Double.parseDouble(degree);
        return degree;
    }
}
