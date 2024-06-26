package com.zybooks.timer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import static android.content.Context.NOTIFICATION_SERVICE;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.app.Notification;
import androidx.core.app.NotificationCompat;


public class TimerWorker extends Worker {

    private final static String CHANNEL_ID_TIMER = "channel_timer";
    private final NotificationManager mNotificationManager;
    public final static String KEY_MILLISECONDS_REMAINING = "com.zybooks.timer.MILLIS_LEFT";
    private final static int NOTIFICATION_ID = 0;

    public TimerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {

        // Get remaining milliseconds from MainActivity
        Data inputData = getInputData();
        long remainingMillis = inputData.getLong(KEY_MILLISECONDS_REMAINING, 0);

        // Can't continue without remaining time
        if (remainingMillis == 0) {
            return Result.failure();
        }

        // Start a new TimerModel
        TimerModel timerModel = new TimerModel();
        timerModel.start(remainingMillis);

        // Create notification channel for all notifications
        createTimerNotificationChannel();

        while (timerModel.isRunning()) {
            try {
                // New notification shows remaining time
                createTimerNotification(timerModel.toString());

                // Wait one second
                Thread.sleep(1000);

                if (timerModel.getRemainingMilliseconds() == 0) {
                    timerModel.stop();

                    // Last notification
                    createTimerNotification("Timer is finished!");
                }
            }
            catch (InterruptedException e) {
                // Ignore
            }
        }

        return Result.success();
    }

    private void createTimerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getApplicationContext().getString(R.string.channel_name);
            String description = getApplicationContext().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_TIMER, name, importance);
            channel.setDescription(description);

            // Register channel with system
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private void createTimerNotification(String text) {
        // Create notification with various properties
        Notification notification = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID_TIMER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        // Post notification
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
}