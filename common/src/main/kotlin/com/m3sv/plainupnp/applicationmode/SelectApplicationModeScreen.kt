package com.m3sv.plainupnp.applicationmode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.m3sv.plainupnp.common.R
import com.m3sv.plainupnp.compose.widgets.*

@Composable
fun SelectApplicationModeScreen(
    initialMode: ApplicationMode,
    onNextClick: (() -> Unit)?,
    onBackClick: (() -> Unit)?,
    nextText: String? = null,
    stringProvider: (Int) -> String,
    onItemSelected: (ApplicationMode) -> Unit,
) {
    OnePane(viewingContent = {
        OneTitle(text = "Select mode")
        OneToolbar(onBackClick = onBackClick) {}
    }) {
        Column(Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
            var selectedMode by remember { mutableStateOf(initialMode) }

            OneSubtitle(text = "Start by selecting theme that you would like to use")
            RadioGroup(
                items = ApplicationMode.values().toList(),
                initial = selectedMode,
                stringProvider = { stringProvider(it.stringValue) },
                onItemSelected = {
                    selectedMode = it
                    onItemSelected(it)
                }
            )

            Spacer(Modifier.weight(1f))

            val text = nextText ?: when (selectedMode) {
                ApplicationMode.Streaming -> stringResource(id = R.string.next)
                ApplicationMode.Player -> stringResource(R.string.finish_onboarding)
            }

            if (onNextClick != null) {
                OneContainedButton(
                    text = text,
                    onClick = onNextClick
                )
            }
        }
    }
}
