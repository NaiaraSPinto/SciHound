package com.jpl.nasa.scihound;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * Class that sets up the notification channels.
 * One channel is for server notifications, and one is for website notifications.
 */
public class Notifications extends Application {
    public static final String CHANNEL_ID = "channel1";
    public static final int NOTIFICATION_ID = 001;
    public static final String CHANNEL2_ID = "channel2";
    public static final int NOTIFICATION2_ID = 002;

    @Override
    public void onCreate() {
        super.onCreate();
        
        createNotificationChannels();
    }

    /**
     * Creates both notification channels.
     * Names the channels and sets the importance of both to high.
     */
    private void createNotificationChannels() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serverChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Point Submissions",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serverChannel.setShowBadge(true);
            serverChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serverChannel);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel server2Channel = new NotificationChannel(
                    CHANNEL2_ID,
                    "Photo Submissions",
                    NotificationManager.IMPORTANCE_HIGH
            );
            server2Channel.setShowBadge(true);
            server2Channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(server2Channel);
        }

    }
}
