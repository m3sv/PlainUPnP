package com.m3sv.plainupnp.presentation.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.m3sv.plainupnp.data.upnp.UriWrapper

@Composable
fun SelectDirectoriesScreen(
    contentUris: List<UriWrapper>,
    pickDirectory: () -> Unit,
    onNext: (() -> Unit)? = null,
    onReleaseUri: (UriWrapper) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(modifier = Modifier.padding(8.dp), text = "Selected directories")
        LazyColumn(content = {
            items(contentUris) { uriWrapper ->
                val dismissState = rememberDismissState(confirmStateChange = {
                    if (it != DismissValue.Default) {
                        onReleaseUri(uriWrapper)
                    }
                    true
                })

                SwipeToDismiss(
                    state = dismissState,
                    background = {
                        val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                        val color by animateColorAsState(targetValue = when (dismissState.targetValue) {
                            DismissValue.Default -> Color.Transparent
                            DismissValue.DismissedToEnd,
                            DismissValue.DismissedToStart,
                            -> Color.Red
                        })

                        val alignment = when (direction) {
                            DismissDirection.StartToEnd -> Alignment.CenterStart
                            DismissDirection.EndToStart -> Alignment.CenterEnd
                        }

                        val icon = Icons.Default.Delete
                        val scale by animateFloatAsState(targetValue = if (dismissState.targetValue == DismissValue.Default)
                            0.75f
                        else
                            1f
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
                    Surface(Modifier
                        .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(4.dp))
                        .padding(
                            top = 2.dp,
                            bottom = 2.dp
                        )
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(4.dp))
                                .padding(16.dp, 8.dp, 16.dp, 8.dp)
                                .height(24.dp),
                            text = "${uriWrapper.uriPermission.uri.path?.split(":")?.last()}")
                    }
                }
            }
        })

        Spacer(Modifier.weight(1f))

        Button(onClick = pickDirectory, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(text = "Pick directory")
        }

        if (onNext != null) {
            Button(onClick = onNext) {
                Text(text = "Start using PlainUPnP")
            }
        }
    }
}
