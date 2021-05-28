package com.m3sv.plainupnp.presentation.onboarding

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.applicationmode.ApplicationMode
import com.m3sv.plainupnp.backgroundmode.BackgroundModeManager
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.common.util.asApplicationMode
import com.m3sv.plainupnp.common.util.pass
import com.m3sv.plainupnp.data.upnp.UriWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private enum class Direction {
    Forward, Backward
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val application: Application,
    private val themeManager: ThemeManager,
    private val backgroundModeManager: BackgroundModeManager,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val imageContainerEnabled = mutableStateOf(false)
    val audioContainerEnabled = mutableStateOf(false)
    val videoContainerEnabled = mutableStateOf(false)

    val backgroundMode = mutableStateOf(backgroundModeManager.backgroundMode)

    val activeTheme: Flow<ThemeOption> = themeManager.theme

    val contentUris: StateFlow<List<UriWrapper>> = preferencesRepository
        .persistedUrisFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, preferencesRepository.getUris())

    private val _currentScreen: MutableSharedFlow<Direction> = MutableSharedFlow()

    val currentScreen: StateFlow<OnboardingScreen> =
        _currentScreen.scan(OnboardingScreen.Greeting) { currentScreen, direction ->
            if (direction == Direction.Forward) {
                when (currentScreen) {
                    OnboardingScreen.SelectPreconfiguredContainers -> {
                        with(preferencesRepository) {
                            setShareImages(imageContainerEnabled.value)
                            setShareVideos(videoContainerEnabled.value)
                            setShareAudio(audioContainerEnabled.value)
                        }
                    }
                    OnboardingScreen.SelectBackgroundMode ->
                        backgroundModeManager.backgroundMode = backgroundMode.value
                    else -> pass
                }
            }

            when (direction) {
                Direction.Forward -> currentScreen.forward()
                Direction.Backward -> currentScreen.backward()
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, OnboardingScreen.Greeting)

    fun onSelectTheme(themeOption: ThemeOption) {
        themeManager.setTheme(themeOption)
    }

    fun onSelectMode(mode: ApplicationMode) {
        viewModelScope.launch {
            preferencesRepository.setApplicationMode(mode)
        }
    }

    fun onNavigateNext() {
        viewModelScope.launch {
            _currentScreen.emit(Direction.Forward)
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch {
            _currentScreen.emit(Direction.Backward)
        }
    }

    fun saveUri() {
        preferencesRepository.updateUris()
    }

    fun releaseUri(uriWrapper: UriWrapper) {
        preferencesRepository.releaseUri(uriWrapper)
    }

    private fun OnboardingScreen.forward(): OnboardingScreen = when (this) {
        OnboardingScreen.Greeting -> OnboardingScreen.SelectTheme
        OnboardingScreen.SelectTheme -> OnboardingScreen.SelectMode
        OnboardingScreen.SelectMode -> when (getApplicationMode()) {
            ApplicationMode.Streaming -> if (hasStoragePermission()) OnboardingScreen.SelectPreconfiguredContainers else OnboardingScreen.StoragePermission
            ApplicationMode.Player -> OnboardingScreen.Finish
        }
        OnboardingScreen.StoragePermission -> OnboardingScreen.SelectPreconfiguredContainers
        OnboardingScreen.SelectPreconfiguredContainers -> OnboardingScreen.SelectDirectories
        OnboardingScreen.SelectDirectories -> OnboardingScreen.Finish
        OnboardingScreen.SelectBackgroundMode -> OnboardingScreen.Finish
        OnboardingScreen.Finish -> error("Can't navigate from finish screen")
    }

    private fun getApplicationMode(): ApplicationMode =
        requireNotNull(preferencesRepository.preferences.value.preferences?.applicationMode?.asApplicationMode())

    private fun OnboardingScreen.backward(): OnboardingScreen = when (this) {
        OnboardingScreen.Greeting -> OnboardingScreen.Greeting
        OnboardingScreen.SelectTheme -> OnboardingScreen.Greeting
        OnboardingScreen.SelectMode -> OnboardingScreen.SelectTheme
        OnboardingScreen.StoragePermission -> OnboardingScreen.SelectMode
        OnboardingScreen.SelectPreconfiguredContainers -> OnboardingScreen.SelectMode
        OnboardingScreen.SelectDirectories -> OnboardingScreen.SelectPreconfiguredContainers
        OnboardingScreen.SelectBackgroundMode -> OnboardingScreen.SelectDirectories
        OnboardingScreen.Finish -> error("Can't navigate from finish screen")
    }

    fun hasStoragePermission(): Boolean = ContextCompat.checkSelfPermission(
        application,
        STORAGE_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val STORAGE_PERMISSION = READ_EXTERNAL_STORAGE
    }
}
