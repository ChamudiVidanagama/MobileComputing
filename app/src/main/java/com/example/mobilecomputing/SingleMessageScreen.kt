package com.example.mobilecomputing

import android.content.Context
import android.net.Uri
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

            navController.navigate(NavigationRoutes.Conversation.route)
        },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Account Settings")
        }
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
