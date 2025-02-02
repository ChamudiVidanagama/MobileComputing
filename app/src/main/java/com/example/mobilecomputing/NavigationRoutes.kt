package com.example.mobilecomputing

sealed class NavigationRoutes(val route: String) {
    object Conversation : NavigationRoutes("conversation")
    object SingleMessage : NavigationRoutes("single_message")
}
