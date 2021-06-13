package com.m3sv.plainupnp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(private val preferencesRepository: PreferencesRepository) {

    private val scope = MainScope()

    private val theme: StateFlow<ThemeOption> =
        preferencesRepository
            .theme
            .stateIn(scope, SharingStarted.Eagerly, ThemeOption.System)

    fun setTheme(mode: ThemeOption) {
        scope.launch { preferencesRepository.setApplicationTheme(mode) }
    }

    @Composable
    fun collectTheme(): State<ThemeOption> = theme.collectAsState()
}
