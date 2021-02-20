package com.m3sv.plainupnp.compose.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OneSubtitle(text: String, textAlign: TextAlign = TextAlign.Start) {
    Text(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp),
        text = text,
        textAlign = textAlign,
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight.Light
    )
}
