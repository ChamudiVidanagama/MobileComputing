package com.example.mobilecomputing.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserProfileViewModel(private val userDao: UserDao) : ViewModel() {
    var userProfile by mutableStateOf<User?>(null)
        private set

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userProfile = userDao.getUser()
        }
    }

    fun saveUserProfile(profile: User) {
        viewModelScope.launch {
            if (userDao.getUser() == null) {
                userDao.insertUser(profile)
            } else {
                userDao.updateUser(profile)
            }
            userProfile = profile
        }
    }
}
