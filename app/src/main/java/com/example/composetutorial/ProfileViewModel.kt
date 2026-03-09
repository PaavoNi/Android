package com.example.composetutorial

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileViewModel(private val userDao: UserDao) : ViewModel() {

    private val weatherApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApiService::class.java)

    private val _weatherState = mutableStateOf<WeatherResponse?>(null)
    val weatherState: State<WeatherResponse?> = _weatherState

    val userData: StateFlow<UserData?> = userDao.getUserData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val messages: StateFlow<List<MessageEntity>> = userDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveUserData(username: String, imageUri: String) {
        viewModelScope.launch {
            val userData = UserData(username = username, imageUri = imageUri)
            userDao.saveUserData(userData)
        }
    }

    fun sendMessage(author: String, body: String) {
        viewModelScope.launch {
            userDao.insertMessage(MessageEntity(author = author, body = body))
        }
    }

    fun fetchWeather() {
        viewModelScope.launch {
            try {
                _weatherState.value = weatherApi.getWeatherData()
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun loadSampleMessagesIfEmpty() {
        viewModelScope.launch {
            if (userDao.getAllMessagesFirst() == null || userDao.getAllMessagesFirst()!!.isEmpty()) {
                SampleData.conversationSample.forEach {
                    userDao.insertMessage(MessageEntity(author = it.author, body = it.body))
                }
            }
        }
    }
}

class ProfileViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}