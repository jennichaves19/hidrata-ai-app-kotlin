package br.com.hidrataai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters

class NightReminderWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        // Pega o valor da água ingerida do SharedPreferences
        val sharedPreferences = applicationContext.getSharedPreferences("water_prefs", Context.MODE_PRIVATE)
        val totalWater = sharedPreferences.getInt("total_water", 0)

        // Verifica se o total de água ingerida é menor que 2000ml
        if (totalWater < 2000) {
            sendNotification("Você ainda não bebeu 2 litros de água hoje. Beba mais água!")
        }

        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria e configura a notificação
        val notification = Notification.Builder(applicationContext, "hydration_channel")
            .setContentTitle("Hidratação Noturna")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Substitua pelo ícone correto
            .build()

        // Envia a notificação
        notificationManager.notify(2, notification)
    }
}
