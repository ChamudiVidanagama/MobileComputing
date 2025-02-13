package com.example.mobilecomputing

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.mobilecomputing.data.AppDatabase
import com.example.mobilecomputing.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

const val CHANNEL_ID = "notification_channel"
const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101

@Composable
fun SingleMessageScreen(navController: NavController) {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val imagePath = remember { mutableStateOf<String?>(null) }

    val db = Room.databaseBuilder(context, AppDatabase::class.java, "app-database").build()
    val userDao = db.userDao()

    val savedUser = remember { mutableStateOf<User?>(null) }

    LaunchedEffect(true) {
        savedUser.value = withContext(Dispatchers.IO) {
            userDao.getUser()
        }
    }

    savedUser.value?.let { user ->
        name.value = user.name
        imagePath.value = user.imageUri
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                val savedPath = saveImageToInternalStorage(context, uri)
                imagePath.value = savedPath
                savedUser.value?.let {
                    val updatedUser = it.copy(imageUri = savedPath)
                    CoroutineScope(Dispatchers.IO).launch {
                        userDao.updateUser(updatedUser)
                    }
                }
            }
        }
    )

    // Permission launcher for requesting notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, send the notification
                triggerNotification(context)
            } else {
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Account Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        imagePath.value?.let { path ->
            val file = File(path)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .align(Alignment.CenterHorizontally)
            ) {
                if (file.exists()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = file),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile_picture),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile_picture),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Enter Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (name.value.isEmpty()) {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@Button
            }

            CoroutineScope(Dispatchers.IO).launch {
                val user = User(name = name.value, imageUri = imagePath.value)
                Log.d("User Saving", "Saving user: ${user.name}, Image Path: ${user.imageUri}")
                userDao.insertUser(user)
            }

            triggerNotification(context)
            navController.navigate(NavigationRoutes.Conversation.route)
        },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Account Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enable Notifications button requests notification permission
        Button(onClick = {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }) {
            Text("Enable Notifications")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test Notification button
//        Button(onClick = { triggerNotification(context) }) {
//            Text("Test Notification")
//        }
    }
}

fun triggerNotification(context: Context) {
    createNotificationChannel(context) // Ensure the channel exists

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(context, "Please grant notification permission", Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE // or FLAG_MUTABLE depending on your case
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Notification Enabled")
        .setContentText("You will be notified when device rotates!")
        .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for banners
        .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Categorize as a message
        .setContentIntent(pendingIntent)  // Action when tapped
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Make it visible to everyone
        .setDefaults(NotificationCompat.DEFAULT_ALL)  // Play sound, vibrate, etc.
        .setLights(0xFF0000, 500, 500)  // Optional: Add light if you want a visual cue
        .build()

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(0, notification)
}



fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Default Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for default notifications"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}


fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(imageUri) ?: return null
    val file = File(context.filesDir, "profile_image.jpg")
    file.outputStream().use { output ->
        inputStream.copyTo(output)
    }
    return file.absolutePath
}
