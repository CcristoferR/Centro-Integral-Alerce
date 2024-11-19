package com.gestionactividades.centrointegralalerce;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Obtener datos enviados al WorkManager
        String title = getInputData().getString("title");
        String message = getInputData().getString("message");

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear un canal de notificación para Android 8.0 o superior
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Notificaciones", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification) // Reemplaza con tu propio ícono
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(1, builder.build());

        return Result.success();
    }
}
