package com.m3sv.plainupnp.presentation.onboarding.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.compose.widgets.*
import com.m3sv.plainupnp.presentation.onboarding.R

@Composable
fun SelectBackgroundModeScreen(
    backgroundMode: MutableState<Boolean>,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    stringProvider: @Composable (Boolean) -> String,
) {
    OnePane(viewingContent = {
        OneTitle(text = "Select background mode")
        OneToolbar(onBackClick = onBackClick) {}
    }) {
        Column {
            OneSubtitle(
                text = "Select whether application can run in background or not",
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            RadioGroup(
                modifier = Modifier.padding(start = 24.dp),
                items = listOf(true, false),
                initial = backgroundMode.value,
                stringProvider = stringProvider
            ) {
                backgroundMode.value = it
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                OneContainedButton(
                    text = stringResource(id = R.string.finish_onboarding),
                    onClick = onNextClick
                )
            }
        }
    }
}
