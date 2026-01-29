package com.example.aura.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.aura.R
import com.example.aura.domain.model.ThemeMode
import com.example.aura.shared.component.AuraCard
import com.example.aura.shared.component.AuraTransparentTopBar
import com.example.aura.shared.core.mvi.CollectSideEffect
import com.example.aura.shared.core.mvi.collectAsState
import com.example.aura.shared.theme.AuraTheme
import com.example.aura.shared.theme.dimens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.CollectSideEffect { effect ->
        when (effect) {
            is SettingsEffect.ShowError -> {
                snackbarHostState.showSnackbar(
                    message = effect.message,
                    withDismissAction = true
                )
            }
            is SettingsEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    SettingsScreenContent(
        state = state,
        onThemeSelected = viewModel::onThemeSelected,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsState,
    onThemeSelected: (ThemeMode) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            AuraTransparentTopBar(title = stringResource(R.string.settings))
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SettingsContent(
                state = state,
                onThemeSelected = onThemeSelected,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AuraTheme {
        SettingsScreenContent(
            state = SettingsState(isLoading = false),
            onThemeSelected = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.dimens.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.sm)
    ) {
        item {
            Text(
                text = stringResource(R.string.appearance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = MaterialTheme.dimens.sm)
            )
        }

        item {
            ThemeSelector(
                selectedTheme = state.themeMode,
                onThemeSelected = onThemeSelected
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    AuraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.dimens.md)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.sm)
        ) {
            Text(
                text = stringResource(R.string.theme_mode),
                style = MaterialTheme.typography.titleSmall
            )

            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (mode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                            ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                            ThemeMode.DARK -> stringResource(R.string.theme_dark)
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    RadioButton(
                        selected = selectedTheme == mode,
                        onClick = { onThemeSelected(mode) }
                    )
                }
            }
        }
    }
}