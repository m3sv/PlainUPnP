package com.m3sv.plainupnp.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m3sv.plainupnp.compose.widgets.*

@Composable
fun StoragePermissionScreen(onBackClick: () -> Unit, onClick: () -> Unit) {
    OnePane(viewingContent = {
        OneTitle(text = "Storage permission")
        OneToolbar(onBackClick = onBackClick)
    }) {
        Column {
            OneSubtitle("To stream your files we need to get storage access permission")
            Spacer(modifier = Modifier.weight(1f))
            OneContainedButton(text = "Grant permission", onClick = onClick)
        }
    }
}
