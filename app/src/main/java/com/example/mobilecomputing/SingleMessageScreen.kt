package com.example.mobilecomputing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun SingleMessageScreen(navController: NavController) {
    // Handle back gesture section was taken from ChatGPT
    BackHandler {
        navController.navigate(NavigationRoutes.Conversation.route) {
            popUpTo(NavigationRoutes.Conversation.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                navController.navigate(NavigationRoutes.Conversation.route) {
                    popUpTo(NavigationRoutes.Conversation.route) { inclusive = true }
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Account Settings",
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(R.drawable.profile_picture),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp) // Increased size
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                .align(Alignment.CenterHorizontally) // Centered
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Lexi Adams", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Homework 2 Screen Development", fontSize = 16.sp)
        }
    }
}
