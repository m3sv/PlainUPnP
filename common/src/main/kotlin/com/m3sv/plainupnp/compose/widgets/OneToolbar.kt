package com.m3sv.plainupnp.compose.widgets

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.common.R

@Composable
fun BoxScope.OneToolbar(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(8.dp)
        .align(Alignment.BottomCenter)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = vectorResource(id = R.drawable.ic_back),
                contentDescription = null
            )
        }
    }
}
