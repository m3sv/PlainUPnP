package com.m3sv.plainupnp.presentation.onboarding

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SelectDirectoriesScreen(contentUris: List<Uri>, pickDirectory: () -> Unit, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select directories")
        contentUris.forEach {
            Row {
                Text("Uri: ${it.path}")
            }
        }
        Button(onClick = pickDirectory) {
            Text(text = "Pick directory")
        }

        Button(onClick = onNext) {
            Text(text = "Next")
        }
    }
}
