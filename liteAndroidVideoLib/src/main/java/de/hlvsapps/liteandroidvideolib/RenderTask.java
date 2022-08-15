/*
 * This is a part of LiteAndroidVideoLib.
 * To see the authors, look at Github for contributors of this file.
 *
 * Copyright 2022  The LiteAndroidVideoLib Authors:
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RenderTask {
    /**
     * Render a Frame or Multiple Frames at a Specific Position in the Video, described by frameInProject
     *
     * @return Should return a List of Frames as Output from this Render. This List should contain all Frames you want to have in the Video, in correct order. Only this Frames are added to the Video. Should never return null.
     */
    @NotNull
    @NonNull
    List<Bitmap> render(Bitmap bitmap0, @Nullable Bitmap bitmap1, int frame);
}
