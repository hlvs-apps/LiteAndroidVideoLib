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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The render
 * @author hlvs-apps
 */
public class Renderer extends Worker {

    public static final String uriSrcExtraDataByteArray="uriSrcExtraData";
    private final Uri src;

    public static final String uriDestExtraDataByteArray="uriDestExtraData";
    private final Uri dest;

    public static final String frameRateExtraData="frameRateExtraData";
    private final double frameRate;

    public static final String notificationInfoExtraDataTempFile="notificationInfoExtraData";
    private final NotificationInfo notificationInfo;

    public static final String startExtraData="startExtraData";
    private final int start;

    public static final String endExtraData="endExtraData";
    private int end;

    private static final String bitrateExtraData="bitrateExtraData";
    private final int bitrate;

    public static final String renderTaskWrapperExtraDataTempFile="renderTaskWrapperExtraDataTempFile";
    private final RenderTaskWrapper renderTask;

    private final Context context;

    public Renderer(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context=context;
        Data inputData=workerParams.getInputData();
        byte[] tmp=inputData.getByteArray(uriSrcExtraDataByteArray);
        if(tmp!=null)
            src=unmarshal(tmp,Uri.CREATOR);
        else
            src=null;
        tmp=inputData.getByteArray(uriDestExtraDataByteArray);
        if(tmp!=null)
            dest=unmarshal(tmp,Uri.CREATOR);
        else
            dest=null;
        frameRate=inputData.getDouble(frameRateExtraData,-1);
        String tempFile=inputData.getString(notificationInfoExtraDataTempFile);
        NotificationInfo notificationInfo1;
        try {
            if (tempFile != null)
                notificationInfo1 = getParcelableFromTempFile(new File(tempFile), NotificationInfo.CREATOR);
            else
                notificationInfo1 = null;
        }catch (IOException ignored){
            notificationInfo1 =null;
        }
        notificationInfo = notificationInfo1;
        start=inputData.getInt(startExtraData,0);
        end=inputData.getInt(endExtraData,-1);
        bitrate=inputData.getInt(bitrateExtraData,-1);
        tempFile=inputData.getString(renderTaskWrapperExtraDataTempFile);
        RenderTaskWrapper renderTask1;
        try {
            if (tempFile != null)
                renderTask1 = getParcelableFromTempFile(new File(tempFile), RenderTaskWrapper.CREATOR);
            else
                renderTask1 = null;
        }catch (IOException ignored){
            renderTask1 =null;
        }
        renderTask = renderTask1;
    }


    @Override
    public void onStopped() {
        super.onStopped();

    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        try{
            Result result=doRealWork();
            if(result instanceof Result.Failure){
                notificationInfo.stop(this,false);
            }
            return result;
        }catch (Exception ignored){
            notificationInfo.stop(this,false);
            return Result.failure();
        }
    }

    @NonNull
    @NotNull
    public Result doRealWork() {
        FFmpegLogCallback.set();
        ContentResolver resolver = getApplicationContext().getContentResolver();
        notificationInfo.start(this,context);
        //try (FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(new FileInputStream(resolver.openFileDescriptor(src, "r").getFileDescriptor()))) {

        try (FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(resolver.openInputStream(src))) {
            Log.d("GetPixelFormat", String.valueOf(fFmpegFrameGrabber.getPixelFormat()));
            fFmpegFrameGrabber.start();
            if(end==-1) {
                end=fFmpegFrameGrabber.getLengthInFrames();
            }
            double graberFrameRate=fFmpegFrameGrabber.getFrameRate();
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(new FileOutputStream(resolver.openFileDescriptor(dest, "w").getFileDescriptor()), fFmpegFrameGrabber.getImageWidth(), fFmpegFrameGrabber.getImageHeight())) {
            //try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(resolver.openOutputStream(dest,"rw"), fFmpegFrameGrabber.getImageWidth(), fFmpegFrameGrabber.getImageHeight())) {
                /*recorder.setAudioCodec(avcodec.AV_CODEC_ID_AMR_NB);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                //recorder.setVideoBitrate(bitrate==-1?fFmpegFrameGrabber.getVideoBitrate():bitrate);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                 */

                AVFormatContext avFormatContext=new AVFormatContext();
                avFormatContext.debug();
                recorder.setFrameRate(frameRate==-1?graberFrameRate:frameRate);
                recorder.setFormat("mp4");
                recorder.setAudioChannels(0);
                recorder.setMetadata(new HashMap<>());
                recorder.start(avFormatContext);
                FrameBuffer buffer=new FrameBuffer(5);
                int frameInProject=0;


                for(int i=start;i<end;i++){
                    notificationInfo.setProgress(this,i,end+5,context);
                    Frame frame=fFmpegFrameGrabber.grabFrame();
                    Pair<Frame,Frame> result=buffer.addFrameToBuffer(frame);
                    if(result!=null){
                        frameInProject++;
                        for(Frame f:renderTask.getTask().render(VideoBitmap.from(result.first),VideoBitmap.from(result.second),frameInProject)){
                            recorder.record(f);
                        }
                    }
                }
                List<Frame> rest=buffer.getRest();
                for(int j=0;(j+1)<rest.size();j++){
                    Frame f0=rest.get(j);
                    Frame f1=rest.get(j+1);
                    frameInProject++;
                    for(Frame f:renderTask.getTask().render(VideoBitmap.from(f0),VideoBitmap.from(f1),frameInProject)){
                        recorder.record(f);
                    }
                }
                recorder.record(rest.get(rest.size()-1));
                recorder.stop();

            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
                fFmpegFrameGrabber.stop();
                return Result.failure();
            }
            fFmpegFrameGrabber.stop();
        } catch (FileNotFoundException | FrameGrabber.Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
        notificationInfo.stop(this,true);
        return Result.success();
    }

    private static class FrameBuffer{
        private final Map<Long,Frame> frames;
        private final int bufferSize;

        private FrameBuffer(int bufferSize) {
            this.frames = new HashMap<>(bufferSize);
            this.bufferSize = bufferSize; }

        @Nullable
        private Pair<Frame,Frame> addFrameToBuffer(Frame frame){
            frames.put(frame.timestamp,frame.clone());
            if(frames.size()==bufferSize){
                SortedSet<Long> sortedSet=new TreeSet<>(frames.keySet());
                Iterator<Long> itr = sortedSet.iterator();
                long index= itr.next();
                long indexOfSecond=itr.next();
                Frame frameFirst=frames.get(index);
                Frame frameSecond=frames.get(indexOfSecond);
                frames.remove(index);
                return Pair.create(frameFirst,frameSecond);
            }
            return null;
        }

        private List<Frame> getRest(){
            ArrayList<Frame> frames=new ArrayList<>();
            SortedSet<Long> sortedSet=new TreeSet<>(this.frames.keySet());
            for(long index:sortedSet){
                frames.add(this.frames.get(index));
                this.frames.remove(index);
            }
            this.frames.clear();
            return frames;
        }

    }

    //From https://gist.github.com/omarmiatello/6711967
    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle(); // not sure if needed or a good idea
        return bytes;
    }

    /**
     * Writes a byte Array to a temp file, which will be deleted at the Apps shutdown
     * @param context Your Context
     * @param array The byte Array
     * @return The File to the Temp file
     * @throws IOException IOException thrown by inner methods
     */
    public static File writeByteArrayToTempFile(Context context, byte[] array) throws IOException {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        File outputFile = File.createTempFile("TmpByteArray", ".bytearray", outputDir);
        try(FileOutputStream stream = new FileOutputStream(outputFile)){
            stream.write(array);
        }
        return outputFile;
    }

    /**
     * Reads a File to an object implementing parcelable.
     * This should only used with temp files, because {@link Parcel} is not Designed for usage with multiple Versions of the Platform.
     * @param file The file
     * @param creator The Creator of the wanted Object
     * @return The Object
     * @throws IOException IOException thrown by inner methods
     * @see Renderer#writeByteArrayToTempFile(Context, byte[])
     */
    public static <T extends Parcelable> T getParcelableFromTempFile(File file,Parcelable.Creator<T> creator) throws IOException {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        return unmarshal(bytes, creator);
    }
    //From https://gist.github.com/omarmiatello/6711967
    public static <T extends Parcelable> T unmarshal(@NonNull byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshal(bytes);
        return creator.createFromParcel(parcel);
    }
    //From https://gist.github.com/omarmiatello/6711967
    public static Parcel unmarshal(@NonNull byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }

    public interface ProgressHandler{
        void handleProgress(int progress,int max);
        void start();
        void stop(boolean finished);
    }
}
