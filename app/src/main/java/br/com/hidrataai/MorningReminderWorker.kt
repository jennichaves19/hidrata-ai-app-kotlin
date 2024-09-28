package br.com.hidrataai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters

class MorningReminderWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        sendNotification("Opa, já se hidratou hoje? Tem que hidratar senão dá pedrinha.")
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Criando a notificação
        val notification = Notification.Builder(applicationContext, "hydration_channel")
            .setContentTitle("Hidratação")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Substitua pelo ícone correto
            .build()

        // Enviando a notificação
        notificationManager.notify(1, notification)
    }
}
