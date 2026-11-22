package com.nirapod.family.child.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.models.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChildMainViewModel(
    private val firebaseRepo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    private val _deviceState = MutableStateFlow<Device?>(null)
    val deviceState: StateFlow<Device?> = _deviceState.asStateFlow()

    fun loadDevice(deviceId: String) {
        viewModelScope.launch {
            firebaseRepo.observeDevice(deviceId).collect {
                _deviceState.value = it
            }
        }
    }
}
