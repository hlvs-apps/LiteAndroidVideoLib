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

package de.hlvsapps.test;

import android.os.Parcel;

import androidx.annotation.NonNull;

import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.List;

import de.hlvsapps.liteandroidvideolib.RenderTask;
import de.hlvsapps.liteandroidvideolib.VideoBitmap;

public class video_test_render_task implements RenderTask {

    @NonNull
    @Override
    public List<Frame> render(VideoBitmap bitmap0, VideoBitmap bitmap1, int frameInProject) {
        ArrayList<Frame> result=new ArrayList<>(1);
        result.add(bitmap0.getFrame());
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator<video_test_render_task> CREATOR = new Creator<video_test_render_task>() {

        @Override
        public video_test_render_task createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public video_test_render_task[] newArray(int size) {
            return new video_test_render_task[0];
        }
    };

}
