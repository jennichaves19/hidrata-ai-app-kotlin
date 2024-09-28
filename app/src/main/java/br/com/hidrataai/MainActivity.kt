package br.com.hidrataai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.work.*
import br.com.hidrataai.ui.theme.HIdrataAíTheme
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel()
        scheduleMorningNotification()
        scheduleNightCheck(this)

        setContent {
            HIdrataAíTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WaterCounterApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminder"
            val descriptionText = "Channel for hydration reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("hydration_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleMorningNotification() {
        val workManager = WorkManager.getInstance(applicationContext)

        val morningRequest = OneTimeWorkRequestBuilder<MorningReminderWorker>()
            .setInitialDelay(calculateDelayForMorning(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueue(morningRequest)
    }

    private fun scheduleNightCheck(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val nightRequest = OneTimeWorkRequestBuilder<NightReminderWorker>()
            .setInitialDelay(calculateDelayForNight(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueue(nightRequest)
    }

    private fun calculateDelayForMorning(): Long {
        val now = Calendar.getInstance()
        val morning = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }
        if (now.after(morning)) {
            morning.add(Calendar.DAY_OF_MONTH, 1)
        }
        return morning.timeInMillis - now.timeInMillis
    }

    private fun calculateDelayForNight(): Long {
        val now = Calendar.getInstance()
        val night = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 30)
        }
        if (now.after(night)) {
            night.add(Calendar.DAY_OF_MONTH, 1)
        }
        return night.timeInMillis - now.timeInMillis
    }
}

@Composable
fun WaterCounterApp(modifier: Modifier = Modifier) {
    var totalWater by remember { mutableStateOf(0) }
    var customWaterInput by remember { mutableStateOf(TextFieldValue("")) }
    var lastResetDate by remember { mutableStateOf(getCurrentDate()) }

    if (lastResetDate != getCurrentDate()) {
        totalWater = 0
        lastResetDate = getCurrentDate()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Hidrata Aí", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)  // Define a altura da caixa para a imagem
        ) {
            Image(
                painter = painterResource(id = R.drawable.pessoatomandoagua),  // Nome da sua imagem sem extensão
                contentDescription = "Imagem de Hidratação",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Imagem de storyset no Freepik",
                fontSize = 8.sp,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)  // Ajuste o padding conforme necessário
                    .offset(y = (-8).dp)  // Ajusta a posição do texto se necessário
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Total de água ingerida hoje: ${totalWater}ml", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WaterButton(amount = 300) { totalWater += it }
            WaterButton(amount = 500) { totalWater += it }
            WaterButton(amount = 1000) { totalWater += it }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = customWaterInput,
            onValueChange = { customWaterInput = it },
            label = { Text("Quantidade personalizada (ml)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val customAmount = customWaterInput.text.toIntOrNull()
            if (customAmount != null) {
                totalWater += customAmount
                customWaterInput = TextFieldValue("") // Limpar campo após adicionar
            }
        }) {
            Text("Adicionar")
        }
    }
}

@Composable
fun WaterButton(amount: Int, onClick: (Int) -> Unit) {
    // Definindo a cor RGB
    val buttonColor = Color(22 / 255f, 203 / 255f, 210 / 255f)  // Valores normalizados entre 0 e 1

    Button(
        onClick = { onClick(amount) },
        shape = CircleShape,
        modifier = Modifier.size(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Text(text = "+${amount}ml", fontSize = 18.sp, color = Color.White)
    }
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return sdf.format(Date())
}

@Preview(showBackground = true)
@Composable
fun WaterCounterAppPreview() {
    HIdrataAíTheme {
        WaterCounterApp()
    }
}
