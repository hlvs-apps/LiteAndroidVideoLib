/*
 * This is a part of LiteAndroidVideoLib.
 * To see the authors, look at Github for contributors of this file.
 *
 * Copyright 2021  The AndroidVideoLib Authors:
 *       AUTHORS.md
 * Unless otherwise noted, this is
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hlvsapps.liteandroidvideolib;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;

import java.io.Closeable;

/**
 * A Frame Bitmap Container
 *
 * @author hlvs-apps
 */
public class VideoBitmap implements Parcelable, Closeable {
    private final Frame frame;
    private Bitmap bitmap=null;
    public VideoBitmap(Frame frame){
        this.frame=frame;
    }

    public static VideoBitmap from(Frame frame){
        return new VideoBitmap(frame);
    }

    protected VideoBitmap(Parcel in) {
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        AndroidFrameConverter converter=new AndroidFrameConverter();
        frame=converter.convert(bitmap);
        converter.close();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(bitmap==null){
            AndroidFrameConverter converter=new AndroidFrameConverter();
            bitmap=converter.convert(frame);
            converter.close();
        }
        dest.writeParcelable(bitmap, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoBitmap> CREATOR = new Creator<VideoBitmap>() {
        @Override
        public VideoBitmap createFromParcel(Parcel in) {
            return new VideoBitmap(in);
        }

        @Override
        public VideoBitmap[] newArray(int size) {
            return new VideoBitmap[size];
        }
    };

    /**
     * Do not use when not necessary.<br>
     * Instead use {@link VideoBitmap#getFrame()}
     * @return The Bitmap converted from the {@link Frame}.
     */
    public Bitmap getBitmap() {
        if(bitmap==null){
            AndroidFrameConverter converter=new AndroidFrameConverter();
            bitmap=converter.convert(frame);
            converter.close();
        }
        return bitmap;
    }

    /**
     * Gets the Frame
     * @return the Frame
     */
    public Frame getFrame(){
        return frame;
    }


    /**
     * Helper Method to convert a Bitmap to a Frame
     * @param bitmap The Bitmap to Convert
     * @return The Converted Bitmap
     * @see AndroidFrameConverter
     */
    public static Frame frameFromBitmap(Bitmap bitmap){
        AndroidFrameConverter converter=new AndroidFrameConverter();
        Frame frame=converter.convert(bitmap);
        converter.close();
        return frame;
    }

    @Override
    public void close() {
        frame.close();
    }
}