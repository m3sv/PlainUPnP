package com.m3sv.plainupnp.presentation.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.compose.widgets.*

@Composable
fun SelectPreconfiguredContainersScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    audioEnabled: MutableState<Boolean>,
    videoEnabled: MutableState<Boolean>,
    imageEnabled: MutableState<Boolean>,
) {
    OnePane(viewingContent = {
        OneTitle(text = stringResource(id = R.string.select_precofigured_containers_title))
        OneToolbar(onBackClick = onBackClick) {}
    }) {
        Column(Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())) {
            OneSubtitle(
                text = "You can select custom directories in the next step",
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp)
            )

            TextCheckbox(
                state = imageEnabled,
                text = stringResource(id = R.string.images),
            )

            TextCheckbox(
                state = videoEnabled,
                text = stringResource(R.string.videos)
            )

            TextCheckbox(
                state = audioEnabled,
                text = stringResource(R.string.audio)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
            ) {
                OneContainedButton(
                    text = stringResource(id = R.string.next),
                    onClick = onNextClick
                )
            }
        }
    }
}

@Composable
private fun TextCheckbox(state: MutableState<Boolean>, text: String) {
    Row(Modifier
        .fillMaxWidth()
        .clickable { state.value = !state.value }
    ) {
        val modifier = Modifier.padding(
            vertical = 8.dp
        )

        Checkbox(checked = state.value, null, modifier.padding(start = 24.dp))
        Text(text, modifier.padding(horizontal = 8.dp))
    }
}
