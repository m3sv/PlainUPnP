package com.m3sv.plainupnp.compose.widgets

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.OneTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .padding(24.dp)
            .align(Alignment.Center),
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h4,
        fontWeight = FontWeight.Light
    )
}
