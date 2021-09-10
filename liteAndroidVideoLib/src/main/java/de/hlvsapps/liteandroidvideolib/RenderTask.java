/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:                               -
 -       https://github.com/hlvs-apps/AndroidVideoLib/blob/master/AUTHORS.md  -
 - Unless otherwise noted, this is                                            -
 - Licensed under the Apache License, Version 2.0 (the "License");            -
 - you may not use this file except in compliance with the License.           -
 - You may obtain a copy of the License at                                    -
 -                                                                            -
 -     http://www.apache.org/licenses/LICENSE-2.0                             -
 -                                                                            -
 - Unless required by applicable law or agreed to in writing, software        -
 - distributed under the License is distributed on an "AS IS" BASIS,          -
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   -
 - See the License for the specific language governing permissions and        -
 - limitations under the License.                                             -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.liteandroidvideolib;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Interface used for Rendering. Your Implementation is the main Part of Rendering a Video.
 * Here you can synchronously modify the Images, the work is done in the RenderTasks.
 * Because the Work is done in the Render Tasks, your class needs to provide a public static final Parcelable.Creator&lt;YourImplementation&gt; CREATOR field.
 * The Implementation MUST be in a class with a Canonical Name, otherwise LiteAndroidVideoLib will throw an IllegalArgumentException.
 * @author hlvs-apps
 * @implNote You MUST PROVIDE A public static final {@link Creator}&lt;YourImplementation&gt; CREATOR field
 * The Implementation MUST be in a class with a Canonical Name, otherwise AndroidVideoLib will throw an IllegalArgumentException.
 */
public interface RenderTask extends Parcelable{
    /**
     * Render a Frame or Multiple Frames at a Specific Position in the Video, described by frameInProject
     * @param videoBitmap0 The VideoBitmap at this Position.
     * @param videoBitmap1 The VideoBitmap at this Position +1.
     * @param frameInProject This Position.
     * @return Should return a List of Frames as Output from this Render. This List should contain all Frames you want to have in the Video, in correct order. Only this Frames are added to the Video. Should never return null.
     */
    @NotNull @NonNull
    List<Frame> render(VideoBitmap videoBitmap0, VideoBitmap videoBitmap1, int frameInProject);
}
