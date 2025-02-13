package com.example.mobilecomputing

import SampleData
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.mobilecomputing.data.AppDatabase
import com.example.mobilecomputing.data.User
import com.example.mobilecomputing.ui.theme.MobileComputingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private var isFirstLaunch = true // Flag to track the first launch
    private var isNotificationTriggered = false // Track if the rotation notification has been triggered
    private var isFirstRotationReading = true // Flag to track if first rotation sensor reading should be ignored
    private val threshold = 10.0 // Rotation threshold to trigger notification
    private var lastRotation: FloatArray? = null // Track the last known rotation to detect manual change
    private var isFirstRotationNotificationSent = false // Flag to track if first rotation alert has been sent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileComputingTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationHost(navController)
                }
            }
        }

        // Initialize sensor manager for rotation vector
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        // Trigger test notification only once on app launch
        if (isFirstLaunch) {
            triggerTestNotification(this)
            isFirstLaunch = false // Ensure the test notification is only triggered once
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                val orientationAngles = FloatArray(3)

                // Get the rotation matrix from the sensor event
                SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)

                // Compute pitch, roll, and azimuth from the rotation matrix
                val pitch = Math.toDegrees(Math.asin(rotationMatrix[6].toDouble())).toFloat()
                val roll = Math.toDegrees(Math.atan2(rotationMatrix[3].toDouble(), rotationMatrix[0].toDouble())).toFloat()
                val azimuth = Math.toDegrees(Math.atan2(rotationMatrix[7].toDouble(), rotationMatrix[8].toDouble())).toFloat()

                // Log values for debugging purposes
                Log.d("RotationSensor", "Azimuth: $azimuth, Pitch: $pitch, Roll: $roll")

                // Skip processing the first reading after launch to avoid false triggering
                if (isFirstRotationReading) {
                    isFirstRotationReading = false
                    return // Skip processing the first sensor reading
                }

                // Avoid triggering the rotation notification immediately after app launch
                if (isFirstRotationNotificationSent) {
                    // Only trigger rotation notification if the device rotation exceeds the threshold
                    val rotationDifference = lastRotation?.let {
                        val diffPitch = Math.abs(pitch - it[0])
                        val diffRoll = Math.abs(roll - it[1])
                        diffPitch > threshold || diffRoll > threshold
                    } ?: true

                    if (rotationDifference && !isNotificationTriggered) {
                        triggerNotification(this, pitch, roll)
                        isNotificationTriggered = true // Set to true after triggering the first notification
                    }
                } else {
                    // Set the flag to true after the first rotation reading
                    isFirstRotationNotificationSent = true
                }

                // Update last rotation for the next comparison
                lastRotation = floatArrayOf(pitch, roll)

                // Reset the notification trigger when the device is back to neutral position
                if (abs(pitch) < threshold && abs(roll) < threshold && isNotificationTriggered) {
                    isNotificationTriggered = false // Allow triggering again if the device is neutral
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    // Function to trigger the test notification on app launch
    fun triggerTestNotification(context: Context) {
        val channelId = "test_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Test Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Test App Launch ")
            .setContentText("Automatic notification on app launch!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }

    // Function to trigger rotation notification when rotation exceeds threshold
    fun triggerNotification(context: Context, pitch: Float, roll: Float) {
        val channelId = "rotation_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rotation Alerts",
                NotificationManager.IMPORTANCE_HIGH // Ensure high importance
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Rotation Alert")
            .setContentText("Whoa, Spinning... Pitch: ${pitch.toInt()}°, Roll: ${roll.toInt()}°")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set high priority
            .setDefaults(Notification.DEFAULT_ALL) // Enable sound and vibration
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(1, notification)
    }
}







data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message, userImageUri: String?) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        val context = LocalContext.current
        val imageView = remember { ImageView(context) }

        userImageUri?.let {
            val painter: Painter = rememberAsyncImagePainter(
                model = userImageUri ?: R.drawable.profile_picture
            )

            Image(
                painter = painter,
                contentDescription = "User Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            )
        } ?: run {
            Image(
                painter = painterResource(R.drawable.profile_picture),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            )
        }


        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier.animateContentSize().padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Conversation(messages: List<Message>, userImageUri: String?) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message, userImageUri)
        }
    }
}

@Preview
@Composable
fun PreviewConversation() {
    MobileComputingTheme {
        val navController = rememberNavController()
        ConversationScreen(navController = navController)
    }
}

@Composable
fun ConversationScreen(navController: NavController) {
    val context = LocalContext.current

    val db = Room.databaseBuilder(context, AppDatabase::class.java, "app-database").build()
    val userDao = db.userDao()

    val savedUser = remember { mutableStateOf<User?>(null) }

    LaunchedEffect(true) {
        savedUser.value = withContext(Dispatchers.IO) {
            userDao.getUser()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Conversation",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        IconButton(
            onClick = { navController.navigate(NavigationRoutes.SingleMessage.route) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(66.dp)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.settings),
                contentDescription = "Next Screen",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(top = 64.dp)) {
            savedUser.value?.let { user ->
                Log.d("Restart App", "User: ${user.name}, Image URI: ${user.imageUri}")
                Conversation(SampleData.conversationSample(user.name), user.imageUri)
            } ?: run {
                val defaultUser = User(name = "Lexi", imageUri = "android.resource://com.example.mobilecomputing/drawable/profile_picture")
                Conversation(SampleData.conversationSample(defaultUser.name), defaultUser.imageUri)
            }
        }

    }
}