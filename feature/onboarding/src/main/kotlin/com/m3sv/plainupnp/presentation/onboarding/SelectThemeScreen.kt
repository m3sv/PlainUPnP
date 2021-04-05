package com.m3sv.plainupnp.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.compose.widgets.*

@Composable
fun SelectThemeScreen(
    titleText: String,
    selectedTheme: ThemeOption,
    stringProvider: (ThemeOption) -> String,
    onThemeOptionSelected: (ThemeOption) -> Unit,
    onClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    OnePane(viewingContent = {
        OneTitle(text = titleText)
        OneToolbar(onBackClick = onBackClick) {}
    }) {
        Column(Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
            OneSubtitle(
                text = "Start by selecting theme that you would like to use",
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            RadioGroup(
                modifier = Modifier.padding(start = 24.dp),
                items = ThemeOption.values().toList(),
                initial = selectedTheme,
                stringProvider = stringProvider,
                onItemSelected = onThemeOptionSelected
            )

            Spacer(Modifier.weight(1f))

            Row(modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
            ) {
                OneContainedButton(text = "Next", onClick = onClick)
            }
        }
    }
}

@Preview
@Composable
private fun SelectableThemePreview() {
    SelectThemeScreen(titleText = "Set theme",
        selectedTheme = ThemeOption.Light,
        stringProvider = { "" },
        onThemeOptionSelected = { /*TODO*/ },
        onClick = { /*TODO*/ }, onBackClick = {})
}
