package android.hardware.camera2.params;

import android.util.Size;

import android.hardware.camera2.utils.HashCodeHelpers;

import androidx.annotation.Keep;

@Keep
public class StreamConfigurationDuration {

    private final int mFormat;
    private final int mWidth;
    private final int mHeight;
    private final long mDurationNs;

    public StreamConfigurationDuration(
            final int format, final int width, final int height, final long durationNs) {
        mFormat =  format;
        mWidth = width;
        mHeight = height;
        mDurationNs = durationNs;
    }

    public final int getFormat() {
        return mFormat;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Size getSize() {
        return new Size(mWidth, mHeight);
    }

    public long getDuration() {
        return mDurationNs;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof StreamConfigurationDuration) {
            final StreamConfigurationDuration other = (StreamConfigurationDuration) obj;
            return mFormat == other.mFormat &&
                    mWidth == other.mWidth &&
                    mHeight == other.mHeight &&
                    mDurationNs == other.mDurationNs;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCodeHelpers.hashCode(mFormat, mWidth, mHeight,
                (int) mDurationNs, (int)(mDurationNs >>> Integer.SIZE));
    }

}
