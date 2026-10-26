package com.moefactory.bettermiuiexpress.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moefactory.bettermiuiexpress.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainScreen(
    isModuleActive: Boolean,
    frameworkName: String,
    frameworkApiLevel: Int,
    yukiVersion: String,
    reloadAvailable: Boolean,
    reloading: Boolean,
    reloadStatus: String,
    onReload: () -> Unit,
    onOpenLink: (String) -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(title = stringResource(R.string.app_name), scrollBehavior = scrollBehavior)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painterResource(if (isModuleActive) R.drawable.ic_active else R.drawable.ic_inactive),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(if (isModuleActive) R.string.active else R.string.inactive),
                                style = MiuixTheme.textStyles.headline1.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            Text(
                                text = if (isModuleActive) stringResource(
                                    R.string.active_hook_framework_version, frameworkName, frameworkApiLevel
                                ) else stringResource(R.string.inactive_description),
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }
            }

            if (isModuleActive) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp)) {
                        Text(
                            stringResource(R.string.module_reload_title),
                            style = MiuixTheme.textStyles.headline1.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = stringResource(if (reloadAvailable) R.string.module_reload_description else R.string.module_reload_unavailable),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )

                        if (reloadStatus.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))

                            Text(reloadStatus, style = MiuixTheme.textStyles.footnote1)
                        }

                        Spacer(Modifier.height(12.dp))

                        TextButton(
                            text = stringResource(if (reloading) R.string.module_reload_running else R.string.module_reload_action),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = reloadAvailable && !reloading,
                            textStyle = MiuixTheme.textStyles.main.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            onClick = onReload
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(16.dp)
                ) {
                    Text(stringResource(R.string.copyright))

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            text = stringResource(R.string.github),
                            modifier = Modifier.weight(1f),
                            textStyle = MiuixTheme.textStyles.main.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            onClick = { onOpenLink("https://github.com/Robotxm/BetterMiuiExpress") }
                        )

                        TextButton(
                            text = stringResource(R.string.blog),
                            modifier = Modifier.weight(1f),
                            textStyle = MiuixTheme.textStyles.main.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            onClick = { onOpenLink("https://moefactory.com") }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp),
                    onClick = { onOpenLink("https://github.com/HighCapable/YukiHook") }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.yuki),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )

                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.yuki),
                                style = MiuixTheme.textStyles.headline1.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                            )

                            Text(
                                stringResource(R.string.yuki_version, yukiVersion),
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScreenPreview() {
    BetterMiuiExpressTheme {
        MainScreen(
            isModuleActive = true,
            frameworkName = "LSPosed",
            frameworkApiLevel = 102,
            yukiVersion = "1.5.0",
            reloadAvailable = true,
            reloading = false,
            reloadStatus = "",
            onReload = {},
            onOpenLink = {}
        )
    }
}
