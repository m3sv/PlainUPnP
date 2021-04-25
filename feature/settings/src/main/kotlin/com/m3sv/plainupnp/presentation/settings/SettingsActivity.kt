package com.m3sv.plainupnp.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m3sv.plainupnp.applicationmode.ApplicationModeManager
import com.m3sv.plainupnp.common.preferences.Preferences
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.common.util.pass
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.compose.widgets.OnePane
import com.m3sv.plainupnp.compose.widgets.OneTitle
import com.m3sv.plainupnp.compose.widgets.OneToolbar
import com.m3sv.plainupnp.presentation.onboarding.activity.ConfigureFolderActivity
import com.m3sv.plainupnp.presentation.onboarding.selecttheme.SelectThemeActivity
import com.m3sv.selectcontentdirectory.SelectApplicationModeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var preferences: PreferencesRepository

    @Inject
    lateinit var applicationModeManager: ApplicationModeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences = preferences.preferences.collectAsState(runBlocking { preferences.preferences.first() })
            SettingsContent(preferences)
        }
    }

    @Composable
    private fun SettingsContent(state: State<Preferences>) {
        AppTheme {
            Surface {
                OnePane(viewingContent = {
                    OneTitle(stringResource(id = R.string.title_feature_settings))
                    OneToolbar(onBackClick = { finish() }) {}
                }) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        ThemeSection(state.value.theme)
                        UpnpSection()
                        AboutSection()
                    }
                }
            }
        }
    }

    @Composable
    fun AboutSection() {
        Section {
            SectionRow(
                title = stringResource(id = R.string.contact_us_title),
                currentValue = stringResource(id = R.string.contact_us_body),
                icon = painterResource(id = R.drawable.ic_baseline_email_green)
            ) {
                openActivity(::openEmail)
            }

            RowDivider()

            SectionRow(
                title = stringResource(id = R.string.rate),
                currentValue = stringResource(id = R.string.open_play_store),
                icon = painterResource(id = R.drawable.ic_play_store)
            ) {
                rateApplication()
            }

            RowDivider()

            SectionRow(
                title = stringResource(id = R.string.github_link_title),
                currentValue = stringResource(id = R.string.source_url),
                painterResource(id = R.drawable.ic_github)
            ) {
                openActivity(::openGithub)
            }

            RowDivider()

            SectionRow(
                title = stringResource(id = R.string.privacy_policy),
                currentValue = stringResource(id = R.string.open_privacy_policy),
                icon = painterResource(id = R.drawable.ic_privacy_policy)
            ) {
                openActivity(::openPrivacyPolicy)
            }

            RowDivider()

            SectionRow(
                title = stringResource(id = R.string.version),
                currentValue = "1.0",
            ) {}
        }
    }

    @Composable
    private fun UpnpSection() {
        val applicationMode by applicationModeManager.applicationMode.collectAsState()

        Section {
            SectionRow(
                title = stringResource(id = R.string.application_mode_settings),
                currentValue = applicationMode?.stringValue?.let { stringResource(id = it) }
            ) {
                startActivity(Intent(applicationContext, SelectApplicationModeActivity::class.java))
            }

            RowDivider()

            SectionRow(
                title = stringResource(R.string.selected_folders),
                icon = painterResource(id = R.drawable.ic_folder_24dp)
            ) {
                startActivity(Intent(applicationContext, ConfigureFolderActivity::class.java))
            }

            RowDivider()

            SectionRow(
                title = stringResource(id = R.string.share_images),
                currentValue = "value",
                icon = painterResource(id = R.drawable.ic_image)
            ) {}

            RowDivider()

            SectionRow(
                title = stringResource(id = R.string.share_videos),
                currentValue = "value",
                icon = painterResource(id = R.drawable.ic_video)
            ) {}

            RowDivider()

            SectionRow(
                title = stringResource(id = R.string.share_music),
                currentValue = "value",
                icon = painterResource(id = R.drawable.ic_music)
            ) {}
        }
    }

    @Composable
    private fun ThemeSection(currentTheme: Preferences.Theme) {
        val textId = when (currentTheme) {
            Preferences.Theme.SYSTEM -> R.string.system_theme_label
            Preferences.Theme.LIGHT -> R.string.light_theme_label
            Preferences.Theme.DARK -> R.string.dark_theme_label
            else -> error("Theme is not set")
        }

        Section {
            SectionRow(
                title = stringResource(R.string.set_theme_label),
                currentValue = stringResource(id = textId),
                icon = painterResource(id = R.drawable.ic_theme)
            ) {
                startActivity(Intent(applicationContext, SelectThemeActivity::class.java))
            }
        }
    }

    @Composable
    fun RowDivider() {
        Divider(Modifier.padding(start = 48.dp, end = 8.dp))
    }

    @Composable
    private fun Section(sectionContent: @Composable (ColumnScope.() -> Unit)) {
        Row {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(content = sectionContent)
            }
        }
    }

    @Composable
    private fun SectionRow(title: String, currentValue: String? = null, icon: Painter? = null, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Image(
                    painter = icon,
                    contentDescription = null,
                    Modifier.size(24.dp)
                )
            }

            Column(Modifier.padding(start = if (icon != null) 16.dp else 4.dp)) {
                Text(title)
                if (currentValue != null) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = currentValue,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }

    private fun openEmail() {
        val email = getString(R.string.dev_email)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("${getString(R.string.mail_to)}$email")
        }

        startActivity(intent)
    }

    private fun rateApplication() {
        try {
            openPlayStore()
        } catch (e: ActivityNotFoundException) {
            openActivity(::openPlayStoreFallback)
        }
    }

    private fun openPlayStore() =
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${getString(R.string.market_prefix)}$packageName")))

    private fun openPlayStoreFallback() {
        Intent(Intent.ACTION_VIEW, Uri.parse("${getString(R.string.play_prefix)}$packageName")).also(::startActivity)
    }

    private fun openPrivacyPolicy() = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(getString(R.string.privacy_policy_link))
    ).also(::startActivity)

    private fun openGithub() =
        Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))).also(::startActivity)

    private fun openActivity(block: () -> Unit) {
        try {
            block()
        } catch (e: ActivityNotFoundException) {
            pass
        }
    }
}
