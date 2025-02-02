package com.example.mobilecomputing

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavigationRoutes.Conversation.route) {
        composable(NavigationRoutes.Conversation.route) { ConversationScreen(navController) }
        composable(NavigationRoutes.SingleMessage.route) { SingleMessageScreen(navController) }
    }
}