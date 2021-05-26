package com.m3sv.plainupnp.compose.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.common.R

@Composable
fun BoxScope.OneToolbar(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.End
    ) {
        if (onBackClick != null) {
            IconButton(modifier = Modifier.padding(8.dp), onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}
