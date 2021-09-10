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

package de.hlvsapps.test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hlvsapps.liteandroidvideolib.NotificationInfo;
import de.hlvsapps.liteandroidvideolib.R;
import de.hlvsapps.liteandroidvideolib.RenderProject;
import de.hlvsapps.liteandroidvideolib.Renderer;


public class video_test extends AppCompatActivity {

    private final int PICK_VIDEO_REQUEST = 190;
    private ProgressBar progressBar;
    private TextView textView;
    private int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout view=new LinearLayout(this);
        setContentView(view.getRootView());

        Button failure=new Button(this);
        failure.setText("Failure");
        Button finish=new Button(this);
        finish.setText("Finish");
        view.addView(failure);
        view.addView(finish);

        failure.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        finish.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });


        Button choose=new Button(this);
        choose.setText("Choose");
        view.addView(choose);
        progressBar=new ProgressBar(this);
        progressBar.setIndeterminate(false);
        view.addView(progressBar);
        textView=new TextView(this);
        view.addView(textView);

        choose.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");

            //intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_VIDEO_REQUEST);
        });

        /*binding.next.setOnClickListener(v -> {
            try {
                if(grab==null){
                    throw new NullPointerException("Grab cannot be null");
                }
                Picture picture = grab.getNativeFrame();
                binding.imageView3.setImageBitmap(AndroidUtil.toBitmap(picture));
            } catch (IOException | NullPointerException e) {
                utils.LogE(e);
            }

        });

         */
    }

    private void updateState(int i, int l){
        Log.d("UpdateState","i: " + i + "; l: " + l);
        runOnUiThread(() -> {
            progressBar.setMax(l);
            progressBar.setProgress(i);
            textView.setText(i + "/" + l);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            //VideoProj videoProj=new VideoProj("Mein_erstes_Testvideo", new Rational(25,1),this);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            String output= getString(R.string.app_name)+"_VideoExport "+formatter.format(now);
            Uri video;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, output);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "TEST_VIDEO");
                video = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            }else{
                String imagesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM).toString() + File.separator + "TEST_VIDEO";

                File file = new File(imagesDir);

                if (!file.exists()) {
                    file.mkdir();
                }

                File videoFile = new File(imagesDir, output + ".mp4");
                video=Uri.fromFile(videoFile);
            }
            RenderProject renderProject=new RenderProject(data.getData(),video,null);
            video_test_render_task task=new video_test_render_task();
            Intent intent = new Intent(this, video_test.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NotificationInfo.PendingIntentForNotificationInfo pendingIntentForNotification=new NotificationInfo.PendingIntentForNotificationInfo(intent,0,0);
            NotificationInfo info=new NotificationInfo("Rendering","Cancel",true,"RENDER_CHANNEL", android.R.drawable.ic_delete,android.R.drawable.ic_delete,pendingIntentForNotification,12146,"RenderChannel","Rendering Notification");
            renderProject.startRender(this, -1, 0, -1, task, info, new Renderer.ProgressHandler() {
                @Override
                public void handleProgress(int progress, int max) {
                    updateState(progress,max);
                }

                @Override
                public void start() {
                    updateState(0,1);
                }

                @Override
                public void stop(boolean finished) {
                    updateState(1,1);
                }
            });
        }
    }
}