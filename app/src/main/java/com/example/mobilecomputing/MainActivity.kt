package com.example.mobilecomputing

import SampleData
import android.annotation.SuppressLint
import android.net.Uri
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.mobilecomputing.data.AppDatabase
import com.example.mobilecomputing.data.User
import com.example.mobilecomputing.ui.theme.MobileComputingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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