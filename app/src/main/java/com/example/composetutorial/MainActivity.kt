package com.example.composetutorial

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.AsyncImage
import com.example.composetutorial.ui.theme.ComposeTutorialTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        val userDao = db.userDao()

        setContent {
            ComposeTutorialTheme {
                MyApp(userDao = userDao)
            }
        }
    }
}

@Composable
fun MyApp(userDao: UserDao) {
    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(userDao))

    LaunchedEffect(Unit) {
        profileViewModel.loadSampleMessagesIfEmpty()
    }

    NavHost(navController = navController, startDestination = "profile_display") {
        composable("profile_input") {
            ProfileInputScreen(navController, profileViewModel)
        }
        composable("profile_display") {
            ProfileDisplayScreen(navController, profileViewModel)
        }
        composable("messages") {
            MessagesScreen(navController, profileViewModel)
        }
        composable("weather") {
            WeatherScreen(navController, profileViewModel)
        }
    }
}

@Composable
fun ProfileInputScreen(navController: NavController, viewModel: ProfileViewModel) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                singlePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Pick Image")
            }
            Spacer(modifier = Modifier.height(16.dp))
            imageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.size(128.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                imageUri?.let { uri ->
                    val newUri = saveImageToInternalStorage(context, uri)
                    viewModel.saveUserData(username, newUri.toString())
                    navController.navigate("profile_display") {
                        popUpTo("profile_input") { inclusive = true }
                    }
                }
            }) {
                Text("Save")
            }
        }
    }
}

@Composable
fun ProfileDisplayScreen(navController: NavController, viewModel: ProfileViewModel) {
    val userData by viewModel.userData.collectAsState()
    val context = LocalContext.current
    val intent = Intent(context, SensorService::class.java)

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            context.startService(intent)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            userData?.let {
                Text("Username: ${it.username}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = Uri.parse(it.imageUri),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("profile_input") }) {
                Text("Edit Profile")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("messages") }) {
                Text("View Messages")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("weather") }) {
                Text("Weather in Oulu")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) -> {
                            context.startService(intent)
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    context.startService(intent)
                }
            }) {
                Text("Start Light Sensor")
            }
        }
    }
}

@Composable
fun MessagesScreen(navController: NavController, viewModel: ProfileViewModel) {
    val messages by viewModel.messages.collectAsState()
    val userData by viewModel.userData.collectAsState()
    var text by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Conversation(messages = messages, user = userData, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                IconButton(onClick = {
                    if (text.isNotBlank()) {
                        viewModel.sendMessage(userData?.username ?: "Me", text)
                        text = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Back to Profile")
            }
        }
    }
}

@Composable
fun WeatherScreen(navController: NavController, viewModel: ProfileViewModel) {
    val weather by viewModel.weatherState

    LaunchedEffect(Unit) {
        viewModel.fetchWeather()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Weather in Oulu", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(24.dp))

            if (weather != null) {
                Text("Temperature: ${weather!!.current.temperature_2m}°C", style = MaterialTheme.typography.headlineMedium)
                Text("Wind Speed: ${weather!!.current.wind_speed_10m} km/h", style = MaterialTheme.typography.bodyLarge)
                Text("Weather Code: ${weather!!.current.weather_code}", style = MaterialTheme.typography.bodyMedium)
            } else {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri): Uri {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.filesDir, "profile_image.jpg")
    val outputStream = file.outputStream()
    inputStream?.copyTo(outputStream)
    return Uri.fromFile(file)
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(author: String, body: String, user: UserData?) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        if (author == user?.username) {
            AsyncImage(
                model = Uri.parse(user.imageUri),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(R.drawable.profile_picture),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = author,
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
                    text = body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Conversation(messages: List<MessageEntity>, user: UserData?, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(messages) { message ->
            MessageCard(author = message.author, body = message.body, user = user)
        }
    }
}
