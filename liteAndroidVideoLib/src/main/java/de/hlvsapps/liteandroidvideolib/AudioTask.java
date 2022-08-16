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

import zeroonezero.android.audio_mixer.AudioMixer;
import zeroonezero.android.audio_mixer.input.AudioInput;

public interface AudioTask {
    /**
     * Configures output Videos audio, using {@link AudioMixer}
     * <br>
     * If you want your output Video to have sound, add at least one {@link AudioInput} to {@code mixer}, for example {@code videoAudio}.
     * <br>
     * To add {@link AudioInput} to {@link AudioMixer}, call {@link AudioMixer#addDataSource(AudioInput)}
     * @param mixer {@link AudioMixer} instance to add all AudioInputs. Is in MixingType {@link zeroonezero.android.audio_mixer.AudioMixer.MixingType#PARALLEL} and must not be changed.
     * @param videoAudio the inputVideos {@link zeroonezero.android.audio_mixer.input.GeneralAudioInput}, or {@link zeroonezero.android.audio_mixer.input.BlankAudioInput} if hasVideoAudio is true
     * @param hasVideoAudio true if inputVideo has Audio, false otherwise
     */
    void configureAudioMixer(AudioMixer mixer, AudioInput videoAudio,boolean hasVideoAudio);
}
