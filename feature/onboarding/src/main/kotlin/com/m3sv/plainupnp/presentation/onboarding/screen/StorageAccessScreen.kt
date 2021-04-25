package com.m3sv.plainupnp.presentation.onboarding.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.compose.widgets.*

@Composable
fun StoragePermissionScreen(onBackClick: () -> Unit, onClick: () -> Unit) {
    OnePane(viewingContent = {
        OneTitle(text = "Storage permission")
        OneToolbar(onBackClick = onBackClick) {}
    }) {
        Column {
            OneSubtitle(
                text = "To stream your files we need to get storage access permission",
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
            ) {
                OneContainedButton(text = "Grant permission", onClick = onClick)
            }
        }
    }
}
