package com.m3sv.plainupnp.applicationmode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.common.R
import com.m3sv.plainupnp.compose.widgets.*

@Composable
fun SelectApplicationModeScreen(
    initialMode: ApplicationMode,
    onNextClick: (() -> Unit)?,
    onBackClick: (() -> Unit)?,
    nextText: String? = null,
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

            OneSubtitle(
                text = "Start by selecting theme that you would like to use",
                Modifier.padding(horizontal = 24.dp)
            )

            RadioGroup(
                modifier = Modifier.padding(start = 24.dp),
                items = ApplicationMode.values().toList(),
                initial = selectedMode,
                stringProvider = { stringResource(it.stringValue) },
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
                Row(Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                ) {
                    OneContainedButton(
                        text = text,
                        onClick = onNextClick
                    )
                }
            }
        }
    }
}
