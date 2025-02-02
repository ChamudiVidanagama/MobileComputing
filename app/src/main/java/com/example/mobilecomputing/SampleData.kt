import com.example.mobilecomputing.Message

/**
 * SampleData for Jetpack Compose Tutorial 
 */
object SampleData {
    fun conversationSample(authorName: String) = listOf(
        Message(authorName, "Test...Test...Test..."),
        Message(authorName, """List of Android versions:
            |Android KitKat (API 19)
            |Android Lollipop (API 21)
            |Android Marshmallow (API 23)
            |Android Nougat (API 24)
            |Android Oreo (API 26)
            |Android Pie (API 28)
            |Android 10 (API 29)
            |Android 11 (API 30)
            |Android 12 (API 31)""".trim()),
        Message(authorName, """I think Kotlin is my favorite programming language.
            |It's so much fun!""".trim()),
        Message(authorName, "Searching for alternatives to XML layouts..."),
        Message(authorName, """Hey, take a look at Jetpack Compose, it's great!
            |It's the Android's modern toolkit for building native UI.
            |It simplifies and accelerates UI development on Android.
            |Less code, powerful tools, and intuitive Kotlin APIs :)""".trim()),
        Message(authorName, "It's available from API 21+ :)"),
        Message(authorName, "Writing Kotlin for UI seems so natural, Compose where have you been all my life?"),
        Message(authorName, "Android Studio next version's name is Arctic Fox"),
        Message(authorName, "Android Studio Arctic Fox tooling for Compose is top notch ^_^"),
        Message(authorName, "I didn't know you can now run the emulator directly from Android Studio"),
        Message(authorName, "Compose Previews are great to check quickly how a composable layout looks like"),
        Message(authorName, "Previews are also interactive after enabling the experimental setting"),
        Message(authorName, "Have you tried writing build.gradle with KTS?")
    )
}

