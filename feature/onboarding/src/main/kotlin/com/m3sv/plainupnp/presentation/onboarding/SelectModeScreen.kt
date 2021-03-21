package com.m3sv.plainupnp.presentation.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m3sv.plainupnp.compose.widgets.*


enum class ApplicationMode(@StringRes val stringValue: Int) {
    Streaming(R.string.application_mode_streaming),
    Player(R.string.application_mode_player),
}

@Composable
fun SelectModeScreen(
    onClick: () -> Unit,
    onBackClick: () -> Unit,
    stringProvider: (Int) -> String,
    onItemSelected: (ApplicationMode) -> Unit,
) {
    OnePane(viewingContent = {
        OneTitle(text = "Select mode")
        OneToolbar(onBackClick = onBackClick)
    }) {
        Column(Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
            OneSubtitle(text = "Start by selecting theme that you would like to use")
            RadioGroup(
                items = ApplicationMode.values().toList(),
                initial = ApplicationMode.Streaming,
                stringProvider = { stringProvider(it.stringValue) },
                onItemSelected = onItemSelected
            )
            Spacer(Modifier.weight(1f))
            OneContainedButton(text = "Next", onClick = onClick)
        }
    }
}
