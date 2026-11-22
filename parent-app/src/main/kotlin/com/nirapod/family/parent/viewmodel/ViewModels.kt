package com.nirapod.family.parent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nirapod.family.shared.auth.AuthRepository
import com.nirapod.family.shared.firebase.FirebaseRepository
import com.nirapod.family.shared.firebase.LicenseRepository
import com.nirapod.family.shared.models.Device
import com.nirapod.family.shared.models.License
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val firebaseRepo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    fun loadDevices(licenseKey: String) {
        viewModelScope.launch {
            firebaseRepo.observeDevicesForParent(licenseKey).collect {
                _devices.value = it
            }
        }
    }
}

class AuthViewModel(
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {
    val currentUser get() = authRepo.currentUser
}

class MonitorViewModel(
    private val firebaseRepo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    // Media monitor controls
}

class LicenseViewModel(
    private val licenseRepo: LicenseRepository = LicenseRepository()
) : ViewModel() {
    private val _license = MutableStateFlow<License?>(null)
    val license: StateFlow<License?> = _license.asStateFlow()

    fun fetchLicense(key: String) {
        viewModelScope.launch {
            val res = licenseRepo.getLicense(key)
            _license.value = res.getOrNull()
        }
    }
}
