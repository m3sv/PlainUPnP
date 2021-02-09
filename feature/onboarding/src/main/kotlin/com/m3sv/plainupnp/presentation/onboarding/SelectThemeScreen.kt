package com.m3sv.plainupnp.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.ThemeOption

@Composable
fun SelectThemeScreen(
    text: String,
    themeOptions: List<ThemeOption>,
    selectedTheme: ThemeOption,
    stringProvider: (Int) -> String,
    onThemeOptionSelected: (ThemeOption) -> Unit,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text)
        RadioGroup(
            themeOptions = themeOptions,
            selectedTheme = selectedTheme,
            stringProvider = stringProvider,
            onThemOptionSelected = onThemeOptionSelected
        )
        Button(onClick = onClick) {
            Text(text = "Next")
        }
    }
}

@Composable
private fun RadioGroup(
    themeOptions: List<ThemeOption>,
    selectedTheme: ThemeOption,
    stringProvider: (Int) -> String,
    onThemOptionSelected: (ThemeOption) -> Unit,
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(selectedTheme) }

    val onClick: (ThemeOption) -> Unit = { themeOption ->
        onOptionSelected(themeOption)
        onThemOptionSelected(themeOption)
    }

    Column {
        themeOptions.forEach { themeOption ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .preferredHeight(56.dp)
                    .selectable(
                        selected = (themeOption == selectedOption),
                        onClick = { onClick(themeOption) }
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The [clearAndSetSemantics] causes the button's redundant
                // selectable semantics to be cleared in favor of the [Row]
                // selectable's, to improve usability with screen-readers.
                Box(Modifier.clearAndSetSemantics {}) {
                    RadioButton(
                        selected = (themeOption == selectedOption),
                        onClick = { onClick(themeOption) }
                    )
                }
                Text(
                    text = stringProvider(themeOption.text),
                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
