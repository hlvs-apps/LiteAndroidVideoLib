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
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.IOException;
import java.util.UUID;

/**
 * Main Class
 * @author hlvs-apps
 */
public class RenderProject implements Parcelable{
    private final Uri pathToOriginalVideo;
    private Uri pathToDestinationVideo;
    private final Parcelable extraData;

    public RenderProject(Uri pathToOriginalVideo, Uri pathToDestinationVideo, Parcelable extraData) {
        this.pathToOriginalVideo = pathToOriginalVideo;
        this.pathToDestinationVideo = pathToDestinationVideo;
        this.extraData = extraData;
    }

    /**
     * Renders the Specified Video asynchronously to the output video, with your Renderer
     * @param c Context of your App, Activity, ...
     * @param frameRate The output frame rate, -1 for the original videos frame rate
     * @param start Start Frame, 0 default
     * @param end End Frame, -1 default
     * @param renderTask Your Render Task
     * @param notificationInfo Notification Info to build Notification
     * @param progressHandler Handle the Progress
     * @return The UUID of the Work
     */
    public UUID startRender(Context c, double frameRate, int start, int end, RenderTask renderTask, NotificationInfo notificationInfo, Renderer.ProgressHandler progressHandler){
        Constraints constraints;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            constraints = new Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .build();
        }else{
            constraints = new Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .build();
        }
        Data.Builder b = new Data.Builder();
        b.putDouble(Renderer.frameRateExtraData, frameRate);
        b.putInt(Renderer.startExtraData, start);
        b.putInt(Renderer.endExtraData, end);
        try {
            b.putString(Renderer.renderTaskWrapperExtraDataTempFile,String.valueOf(Renderer.writeByteArrayToTempFile(c,Renderer.marshall(RenderTaskWrapper.fromRenderTask(renderTask)))));
            b.putString(Renderer.notificationInfoExtraDataTempFile,String.valueOf(Renderer.writeByteArrayToTempFile(c,Renderer.marshall(notificationInfo))));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        b.putByteArray(Renderer.uriSrcExtraDataByteArray,Renderer.marshall(pathToOriginalVideo));
        b.putByteArray(Renderer.uriDestExtraDataByteArray,Renderer.marshall(pathToDestinationVideo));

        OneTimeWorkRequest renderRequest = new OneTimeWorkRequest.Builder(Renderer.class)
                .setConstraints(constraints)
                .setInputData(b.build())
                .build();
        String workerName="RendererWithUUID"+UUID.randomUUID();
        WorkManager.getInstance(c.getApplicationContext()).enqueueUniqueWork(workerName,
                ExistingWorkPolicy.REPLACE, renderRequest);

        WorkManager.getInstance(c.getApplicationContext())
                .getWorkInfoByIdLiveData(renderRequest.getId())
                .observeForever(workInfo -> {
                    Data progressData=workInfo.getProgress();
                    int methodToCall=progressData.getInt(NotificationInfo.keyMethodNameCalledForProgress,-1);
                    if(methodToCall!=-1){
                        NotificationInfo.MethodNameCalledForProgress method=NotificationInfo.MethodNameCalledForProgress.values()[methodToCall];
                        switch (method){
                            case STOP:
                                boolean finished=progressData.getBoolean(NotificationInfo.keyFinished,false);
                                progressHandler.stop(finished);
                                break;
                            case START:
                                progressHandler.start();
                                break;
                            case UPDATE:
                                int status=progressData.getInt(NotificationInfo.keyStatus,-1);
                                int max=progressData.getInt(NotificationInfo.keyMax,-1);
                                progressHandler.handleProgress(status,max);
                                break;
                        }
                    }
                })
        ;

        return renderRequest.getId();
    }


    public void setPathToDestinationVideo(Uri pathToDestinationVideo) {
        this.pathToDestinationVideo = pathToDestinationVideo;
    }

    public Uri getPathToOriginalVideo() {
        return pathToOriginalVideo;
    }

    public Uri getPathToDestinationVideo() {
        return pathToDestinationVideo;
    }

    public Parcelable getExtraData() {
        return extraData;
    }

    protected RenderProject(Parcel in) {
        Parcelable extraData1;
        pathToOriginalVideo = in.readParcelable(Uri.class.getClassLoader());
        pathToDestinationVideo = in.readParcelable(Uri.class.getClassLoader());
        String renderTaskClassName=in.readString();
        if(!renderTaskClassName.equals("-1")) {
            try {
                extraData1 = in.readParcelable(Class.forName(renderTaskClassName).getClassLoader());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                extraData1 = null;
            }
            extraData = extraData1;
        }else{
            extraData=null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(pathToOriginalVideo, flags);
        dest.writeParcelable(pathToDestinationVideo, flags);
        if(extraData!=null) {
            String extraDataClassName = extraData.getClass().getCanonicalName();
            if (extraDataClassName == null) {
                throw new IllegalArgumentException("A extraData Implementation must have a CanonicalName, you can get the CanonicalName by (Your Class Instance).getClass().getCanonicalName()");
            }
            dest.writeString(extraDataClassName);
            dest.writeParcelable(extraData, flags);
        }
        else
            dest.writeString("-1");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RenderProject> CREATOR = new Creator<RenderProject>() {
        @Override
        public RenderProject createFromParcel(Parcel in) {
            return new RenderProject(in);
        }

        @Override
        public RenderProject[] newArray(int size) {
            return new RenderProject[size];
        }
    };

    public static double getMP4LengthInSeconds(Context c, Uri src) throws IOException {
        ContentResolver resolver = c.getApplicationContext().getContentResolver();
        double time;
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(resolver.openInputStream(src));
        ff.start();
        time = ff.getLengthInTime()/ 1_000_000.0;
        ff.stop();
        return time;
    }

    public double getSrcLengthInSeconds(Context context) throws IOException {
        return getMP4LengthInSeconds(context,pathToOriginalVideo);
    }

    public static int getMP4LengthInFrames(Context c,Uri src) throws IOException {
        ContentResolver resolver = c.getApplicationContext().getContentResolver();
        int frames;
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(resolver.openInputStream(src));
        ff.start();
        frames = ff.getLengthInFrames();
        ff.stop();
        return frames;
    }

    public int getSrcLengthInFrames(Context context) throws IOException {
        return getMP4LengthInFrames(context,pathToOriginalVideo);
    }

    public static double getMP4FrameRate(Context c,Uri src) throws IOException {
        ContentResolver resolver = c.getApplicationContext().getContentResolver();
        double frames;
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(resolver.openInputStream(src));
        ff.start();
        frames = ff.getFrameRate();
        ff.stop();
        return frames;
    }

    public double getSrcFrameRate(Context context) throws IOException {
        return getMP4FrameRate(context,pathToOriginalVideo);
    }
}
