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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.compose.widgets.*
import com.m3sv.plainupnp.data.upnp.UriWrapper


@Composable
fun SelectFoldersScreen(
    contentUris: List<UriWrapper>,
    selectDirectory: () -> Unit,
    onBackClick: () -> Unit,
    onNext: (() -> Unit)? = null,
    onReleaseUri: (UriWrapper) -> Unit,
) {
    OnePane(viewingContent = {
        OneTitle(text = "Selected directories")
        OneToolbar(onBackClick = onBackClick)
    }) {
        Column {
            OneSubtitle(text = "Here you can select any custom directories from your file system or SD card")

            LazyColumn(modifier = Modifier
                .weight(1f, true)
                .padding(vertical = 8.dp), content = {
                items(contentUris) { uriWrapper ->
                    var unread by remember { mutableStateOf(false) }
                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            if (it == DismissValue.DismissedToEnd) {
                                unread = !unread
                            }
                            onReleaseUri(uriWrapper)

                            it != DismissValue.DismissedToEnd
                        }
                    )


                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                            val color by animateColorAsState(targetValue = when (dismissState.targetValue) {
                                DismissValue.Default -> Color(0xffff4d4d)
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
                        Surface(shape = RoundedCornerShape(8.dp)) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .height(24.dp),
                                text = "${uriWrapper.uriPermission.uri.path?.split(":")?.last()}")
                        }
                    }
                }
            })


            if (onNext != null) {
                OneOutlinedButton(text = "Pick folder", onClick = selectDirectory)
                OneContainedButton(text = "Start using PlainUPnP", onClick = onNext)
            } else {
                OneContainedButton(text = "Pick folder", onClick = selectDirectory)
            }
        }
    }
}
