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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A wrapper for a {@link RenderTask}, for Parceling
 *
 * @author hlvs-apps
 */
public final class RenderTaskWrapper implements Parcelable {
    private final RenderTask task;

    private RenderTaskWrapper(RenderTask task) {
        this.task = task;
    }

    protected RenderTaskWrapper(Parcel in) {
        String taskClassName=in.readString();
        RenderTask task1;
        if(!taskClassName.equals("-1")) {
            try {
                task1 = in.readParcelable(Class.forName(taskClassName).getClassLoader());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                task1 = null;
            }
            task = task1;
        }else{
            task=null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(task!=null) {
            String taskClassName = task.getClass().getCanonicalName();
            if (taskClassName == null) {
                throw new IllegalArgumentException("A task Implementation must have a CanonicalName, you can get the CanonicalName by (Your Class Instance).getClass().getCanonicalName()");
            }
            dest.writeString(taskClassName);
            dest.writeParcelable(task, flags);
        }
        else {
            dest.writeString("-1");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RenderTaskWrapper> CREATOR = new Creator<RenderTaskWrapper>() {
        @Override
        public RenderTaskWrapper createFromParcel(Parcel in) {
            return new RenderTaskWrapper(in);
        }

        @Override
        public RenderTaskWrapper[] newArray(int size) {
            return new RenderTaskWrapper[size];
        }
    };

    public static RenderTaskWrapper fromRenderTask(RenderTask renderTask){
        return new RenderTaskWrapper(renderTask);
    }

    public RenderTask getTask() {
        return task;
    }


}
