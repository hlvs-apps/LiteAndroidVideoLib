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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

/**
 * Helper class to create a Notification
 *
 * @author hlvs-apps
 */
public class NotificationInfo implements Parcelable {

    enum MethodNameCalledForProgress {
        START, UPDATE, STOP
    }

    static final String keyMethodNameCalledForProgress = "keyMethodNameCalledForProgress";
    static final String keyFinished = "keyFinished";
    static final String keyStatus = "keyStatus";
    static final String keyMax = "keyMax";

    protected NotificationInfo(Parcel in) {
        titleOfNotification = in.readString();
        cancelString = in.readString();
        addCancelOption = in.readByte() != 0;
        CHANNEL_ID = in.readString();
        notificationIcon = in.readInt();
        cancelIcon = in.readInt();
        pendingIntentForNotification = in.readParcelable(PendingIntentForNotificationInfo.class.getClassLoader());
        NOTIFICATION_ID = in.readInt();
        foregroundServiceType = in.readInt();
        nameOfChannel = in.readString();
        descriptionOfChannel = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(titleOfNotification);
        dest.writeString(cancelString);
        dest.writeByte((byte) (addCancelOption ? 1 : 0));
        dest.writeString(CHANNEL_ID);
        dest.writeInt(notificationIcon);
        dest.writeInt(cancelIcon);
        dest.writeParcelable(pendingIntentForNotification, flags);
        dest.writeInt(NOTIFICATION_ID);
        dest.writeInt(foregroundServiceType);
        dest.writeString(nameOfChannel);
        dest.writeString(descriptionOfChannel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NotificationInfo> CREATOR = new Creator<NotificationInfo>() {
        @Override
        public NotificationInfo createFromParcel(Parcel in) {
            return new NotificationInfo(in);
        }

        @Override
        public NotificationInfo[] newArray(int size) {
            return new NotificationInfo[size];
        }
    };

    public NotificationInfo(
            String titleOfNotification,
            String cancelString,
            boolean addCancelOption,
            String channel_id,
            int notificationIcon,
            int cancelIcon,
            PendingIntentForNotificationInfo pendingIntentForNotification,
            int notification_id,
            int foregroundServiceType,
            String nameOfChannel,
            String descriptionOfChannel) {
        this.titleOfNotification = titleOfNotification;
        this.cancelString = cancelString;
        this.addCancelOption = addCancelOption;
        CHANNEL_ID = channel_id;
        this.notificationIcon = notificationIcon;
        this.cancelIcon = cancelIcon;
        this.pendingIntentForNotification = pendingIntentForNotification;
        NOTIFICATION_ID = notification_id;
        this.foregroundServiceType = foregroundServiceType;
        this.nameOfChannel = nameOfChannel;
        this.descriptionOfChannel = descriptionOfChannel;
    }


    public NotificationInfo(
            String titleOfNotification,
            String cancelString,
            boolean addCancelOption,
            String channel_id,
            int notificationIcon,
            int cancelIcon,
            PendingIntentForNotificationInfo pendingIntentForNotification,
            int notification_id,
            String nameOfChannel,
            String descriptionOfChannel) {
        this(titleOfNotification,
                cancelString, addCancelOption,
                channel_id,
                notificationIcon,
                cancelIcon,
                pendingIntentForNotification,
                notification_id,
                0,
                nameOfChannel,
                descriptionOfChannel);
    }

    public final String titleOfNotification;
    public final String cancelString;
    public final boolean addCancelOption;
    public final String CHANNEL_ID;
    public final int notificationIcon;
    public final int cancelIcon;
    public final PendingIntentForNotificationInfo pendingIntentForNotification;
    public final int NOTIFICATION_ID;
    public final int foregroundServiceType;
    public final String nameOfChannel;
    public final String descriptionOfChannel;


    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel(Context context) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, nameOfChannel, importance);
        channel.setDescription(descriptionOfChannel);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static final class PendingIntentForNotificationInfo implements Parcelable {
        private final Intent intentForPendingIntent;
        private final int requestCode;
        private final int flags;

        public PendingIntentForNotificationInfo(Intent intentForPendingIntent, int requestCode, int flags) {
            this.intentForPendingIntent = intentForPendingIntent;
            this.requestCode = requestCode;
            this.flags = flags;
        }

        private PendingIntentForNotificationInfo(Parcel in) {
            intentForPendingIntent = in.readParcelable(Intent.class.getClassLoader());
            requestCode = in.readInt();
            flags = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(intentForPendingIntent, flags);
            dest.writeInt(requestCode);
            dest.writeInt(flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<PendingIntentForNotificationInfo> CREATOR = new Creator<PendingIntentForNotificationInfo>() {
            @Override
            public PendingIntentForNotificationInfo createFromParcel(Parcel in) {
                return new PendingIntentForNotificationInfo(in);
            }

            @Override
            public PendingIntentForNotificationInfo[] newArray(int size) {
                return new PendingIntentForNotificationInfo[size];
            }
        };

        public PendingIntent getPendingIntent(Context context) {
            return PendingIntent.getActivity(context, requestCode, intentForPendingIntent, flags);
        }
    }

}
