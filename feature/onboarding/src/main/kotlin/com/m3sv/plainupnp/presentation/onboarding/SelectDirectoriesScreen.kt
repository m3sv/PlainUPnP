package com.m3sv.plainupnp.presentation.onboarding

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SelectDirectoriesScreen(contentUris: List<Uri>, pickDirectory: () -> Unit, onNext: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select directories")
        contentUris.forEach {
            val dismissState = rememberDismissState()
            SwipeToDismiss(state = dismissState, background = {
                val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                val color by animateColorAsState(targetValue = when (dismissState.targetValue) {
                    DismissValue.Default -> Color.LightGray
                    DismissValue.DismissedToEnd,
                    DismissValue.DismissedToStart,
                    -> Color.Red
                })

                val alignment = when (direction) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                }

                val icon = Icons.Default.Delete
                val scale by animateFloatAsState(targetValue = if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        icon,
                        contentDescription = "Localized description",
                        modifier = Modifier.scale(scale)
                    )
                }
            }) {
                Row(Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Uri: ${it.path}")
                }
            }
        }
        Button(onClick = pickDirectory) {
            Text(text = "Pick directory")
        }

        if (onNext != null) {
            Button(onClick = onNext) {
                Text(text = "Start using PlainUPnP")
            }
        }
    }
}
