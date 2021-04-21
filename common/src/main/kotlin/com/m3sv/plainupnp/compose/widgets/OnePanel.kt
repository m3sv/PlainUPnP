package com.m3sv.plainupnp.compose.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun OnePane(
    viewingContent: @Composable BoxScope.() -> Unit,
    interactionContent: @Composable BoxScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(40f),
            content = viewingContent
        )
        Box(
            modifier = Modifier.weight(60f),
            content = interactionContent
        )
    }
}
