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

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.homesoft.encoder.FileOrParcelFileDescriptor;
import com.homesoft.encoder.Muxer;
import com.homesoft.encoder.MuxerConfig;
import com.homesoft.encoder.MuxerJavaConfiguration;
import com.homesoft.encoder.MuxingPending;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import de.hlvsapps.liteandroidvideolib.decodeeditencode.VideoFrameExtractor;
import zeroonezero.android.audio_mixer.AudioMixer;
import zeroonezero.android.audio_mixer.input.AudioInput;
import zeroonezero.android.audio_mixer.input.BlankAudioInput;
import zeroonezero.android.audio_mixer.input.GeneralAudioInput;

public class RendererRunnable implements Runnable {

    private final Context activityContext;
    private final Uri inputUri;
    private final UriOrFile outputUri;
    private int videoWidth;
    private int videoHeight;
    private final int framesPerImage;
    private int bitrate;
    private final int iFrameIntervall;
    private float framesPerSecond;
    private final RenderTask renderTask;
    private final AudioTask audioTask;
    private ProgressHandler progressHandler;

    public RendererRunnable(Context activityContext, Uri inputUri, UriOrFile outputUri, int videoWidth, int videoHeight, int framesPerImage, int bitrate, int iFrameIntervall, float framesPerSecond, RenderTask renderTask, AudioTask audioTask) {
        this.activityContext = activityContext;
        this.inputUri = inputUri;
        this.outputUri = outputUri;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.framesPerImage = framesPerImage;
        this.bitrate = bitrate;
        this.iFrameIntervall = iFrameIntervall;
        this.framesPerSecond = framesPerSecond;
        this.renderTask = renderTask;
        this.audioTask = audioTask;
    }

    public RendererRunnable(Context activityContext, Uri inputUri, UriOrFile outputUri, int videoWidth, int videoHeight, int framesPerImage, int bitrate, int iFrameIntervall, float framesPerSecond, RenderTask renderTask) {
        this(activityContext,inputUri,outputUri,videoWidth,videoHeight,framesPerImage,bitrate,iFrameIntervall,framesPerSecond,renderTask,null);
    }

    public void setProgressHandler(ProgressHandler progressHandler) {
        this.progressHandler = progressHandler;
    }

    @Override
    public void run() {
        try (ParcelFileDescriptor parcelFileDescriptor = activityContext.getContentResolver().openFileDescriptor(inputUri, "r")) {

            MediaFormat format = VideoFrameExtractor.getVideoInformation(parcelFileDescriptor);
            boolean hasAudioTrack=VideoFrameExtractor.hasAudioTrack(parcelFileDescriptor);
            if (format == null) return;
            int originalVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
            int originalVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);

            try {
                int originalBitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);
                if (bitrate == -1) {
                    bitrate = originalBitrate;
                }
            } catch (NullPointerException ignored) {
                if (bitrate == -1) {
                    bitrate = 1000 * 5000;
                }
            }

            if (videoWidth == -1 || videoHeight == -1) {
                videoWidth = originalVideoWidth;
                videoHeight = originalVideoHeight;
            }

            int rotate = 0;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    rotate = format.getInteger(MediaFormat.KEY_ROTATION);
                }
                if (!(rotate == 0 || rotate == 180)) {
                    int w = videoWidth;
                    videoWidth = videoHeight;
                    videoHeight = w;
                }
            } catch (NullPointerException ignored) {
            }
            long length = format.getLong(MediaFormat.KEY_DURATION);
            long lengthInSeconds = length / 1000000;
            int fps = format.getInteger(MediaFormat.KEY_FRAME_RATE);

            long lengthInFrames = (fps * length) / 1000000;

            if (framesPerSecond <= 0) {
                framesPerSecond = fps;
            }


            MuxerConfig muxerConfig = new MuxerJavaConfiguration(outputUri.getFileOrParcelFileDescriptor(activityContext.getApplicationContext()), videoWidth, videoHeight, framesPerImage, framesPerSecond, bitrate, iFrameIntervall);
            final FrameBuffer buf = new FrameBuffer();
            final Muxer muxer = new Muxer(activityContext.getApplicationContext(), muxerConfig);
            VideoFrameExtractor.BitmapHandler handler = (bitmap, numOfFrame) -> {
                Pair<Bitmap, Bitmap> result;
                if (bitmap != null)
                    result = buf.addFrameToBuffer(numOfFrame, bitmap);
                else result = buf.getRest();
                if (result != null) {
                    for (Bitmap r : renderTask.render(result.first, result.second, numOfFrame)) {
                        muxer.muxFrame(r);
                        r.recycle();
                    }
                    result.first.recycle();
                    if (bitmap == null) {
                        for (Bitmap r : renderTask.render(result.second, null, numOfFrame + 1)) {
                            muxer.muxFrame(r);
                            r.recycle();
                        }
                        result.second.recycle();
                    }
                }
                if (progressHandler != null)
                    progressHandler.handleProgress(numOfFrame, lengthInFrames);
            };
            VideoFrameExtractor extractor = new VideoFrameExtractor();
            AudioMixer audioMixer = null;
            AudioInput videoAudio;
            if(audioTask!=null){
                audioMixer = new AudioMixer(muxerConfig.getFrameMuxer().getMediaMuxer());
                audioMixer.setSampleRate(48000);
                audioMixer.setBitRate(320000);
                audioMixer.setChannelCount(2);
                audioMixer.setMixingType(AudioMixer.MixingType.PARALLEL);
                if(hasAudioTrack)
                    videoAudio = new GeneralAudioInput(parcelFileDescriptor.getFileDescriptor());
                else
                    videoAudio = new BlankAudioInput(length);
                audioTask.configureAudioMixer(audioMixer,videoAudio,hasAudioTrack);
                if(hasAudioTrack) {
                    BlankAudioInput input=new BlankAudioInput(videoAudio.getDurationUs());
                    input.setVolume(0f);
                    audioMixer.addDataSource(input);
                }
                if (progressHandler !=null)
                    audioMixer.setProcessingListener(new AudioMixer.ProcessingListener() {
                        @Override
                        public void onProgress(double progress) {
                            progressHandler.handleAudioProgress(progress);
                        }

                        @Override
                        public void onEnd() {
                            progressHandler.handleAudioFinish();
                        }
                    });
                audioMixer.start();
            }
            if (muxer.prepareMuxingFrameByFrame() instanceof MuxingPending) {
                extractor.extractMpegFrames(parcelFileDescriptor, handler, videoWidth, videoHeight, rotate);
                handler.handleBitmap(null, (int) lengthInFrames - 1);
                if(audioMixer!=null)
                    muxer.endMuxingFrameByFrame(audioMixer::processSync);
                else muxer.endMuxingFrameByFrame();
                if (progressHandler!=null)
                    progressHandler.handleFinish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ProgressHandler {
        void handleProgress(long progress, long total);

        /**
         * Only called when everything successful
         */
        default void handleFinish(){
        }
        default void handleAudioFinish(){
        }
        default void handleAudioProgress(double progress){
            if(BuildConfig.DEBUG){
                Log.d("Render/AudioProcessing","Audio Processing Progress:"+progress+"    (info:only in debug version)");
            }
        }
    }


    public static class UriOrFile {
        private final Uri uri;
        private final File file;

        @RequiresApi(Build.VERSION_CODES.O)
        public UriOrFile(Uri uri) {
            this.uri = uri;
            this.file = null;
        }

        public UriOrFile(File file) {
            this.uri = null;
            this.file = file;
        }

        public FileOrParcelFileDescriptor getFileOrParcelFileDescriptor(Context context) throws FileNotFoundException {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (uri != null)
                    return new FileOrParcelFileDescriptor(context.getContentResolver().openFileDescriptor(uri, "w"));
                else return new FileOrParcelFileDescriptor(Objects.requireNonNull(file));
            } else {
                return new FileOrParcelFileDescriptor(Objects.requireNonNull(file));
            }
        }
    }


    private static class FrameBuffer {
        private final Map<Integer, Bitmap> frames;

        private FrameBuffer() {
            this.frames = new HashMap<>(3);
        }

        @Nullable
        private Pair<Bitmap, Bitmap> addFrameToBuffer(int num, Bitmap image) {
            frames.put(num, image);
            if (frames.size() == 3) {
                SortedSet<Integer> sortedSet = new TreeSet<>(frames.keySet());
                Iterator<Integer> itr = sortedSet.iterator();
                int index = itr.next();
                int indexOfSecond = itr.next();
                Bitmap b0 = frames.get(index);
                Bitmap b1 = frames.get(indexOfSecond);
                frames.remove(index);
                return Pair.create(b0, b1);
            }
            return null;
        }

        private Pair<Bitmap, Bitmap> getRest() {
            SortedSet<Integer> sortedSet = new TreeSet<>(this.frames.keySet());
            Pair<Bitmap, Bitmap> rest = new Pair<>(frames.get(sortedSet.first()), frames.get(sortedSet.last()));
            this.frames.clear();
            return rest;
        }

    }
}
