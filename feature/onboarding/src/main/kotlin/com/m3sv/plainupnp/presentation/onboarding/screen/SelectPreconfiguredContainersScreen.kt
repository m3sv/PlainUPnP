package com.m3sv.plainupnp.presentation.onboarding.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.compose.widgets.*
import com.m3sv.plainupnp.presentation.onboarding.R

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

            SwitchRow(
                checkedState = imageEnabled,
                title = stringResource(id = R.string.images),
            ) {

            }

            SwitchRow(
                checkedState = videoEnabled,
                title = stringResource(R.string.videos)
            ) {

            }

            SwitchRow(
                checkedState = audioEnabled,
                title = stringResource(R.string.audio)
            ) {}

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
private fun SwitchRow(
    title: String,
    checkedState: MutableState<Boolean>,
    icon: Painter? = null,
    onSwitch: (Boolean) -> Unit,
) {

    fun flipSwitch() {
        checkedState.value = !checkedState.value
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                flipSwitch()
                onSwitch(checkedState.value)
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                painter = icon,
                contentDescription = null,
                Modifier.size(24.dp)
            )
        }

        Row(Modifier.padding(start = if (icon != null) 16.dp else 4.dp)) {
            Text(title)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = checkedState.value, onCheckedChange = {
                checkedState.value = it
                onSwitch(it)
            })
        }
    }
}
