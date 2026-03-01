package me.kaylunasa.tahanapp.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.activity.MainActivity;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NotNull Context context, @NotNull WorkerParameters params) {
        super(context, params);
    }

    @NotNull
    @Override
    public Result doWork() {
        Data input = getInputData();
        String childName = input.getString("childName");
        int notifId = input.getInt("notifId", 0);

        showNotification(childName, notifId);
        return Result.success();
    }

    private void showNotification(String childName, int notifId) {
        String channelId = "reminder_channel";

        NotificationManager notifManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "TahanApp Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notifManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0)
        );

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(getApplicationContext().getResources().getText(R.string.notification_title))
                .setContentText(String.format(
                        getApplicationContext().getResources().getText(R.string.notification_content).toString(),
                        childName
                ))
                .setSmallIcon(R.drawable.tahanapp_logo)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notifManager.notify(1, notification.build());
    }
}
