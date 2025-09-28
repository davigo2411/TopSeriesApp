package com.example.topseriesapp.ui.configuration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.topseriesapp.R


enum class ThemeSetting { LIGHT, DARK, SYSTEM_DEFAULT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    currentThemeSetting: ThemeSetting,
    onThemeSettingChanged: (ThemeSetting) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.configuration_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPaddingScaffoldInterno ->
        Column(
            modifier = modifier
                .padding(innerPaddingScaffoldInterno)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.theme_settings_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemeOptionCircle(
                    color = Color.White,
                    isSelected = currentThemeSetting == ThemeSetting.LIGHT,
                    onClick = { onThemeSettingChanged(ThemeSetting.LIGHT) }
                )

                ThemeOptionCircle(
                    color = Color.Black,
                    isSelected = currentThemeSetting == ThemeSetting.DARK,
                    onClick = { onThemeSettingChanged(ThemeSetting.DARK) }
                )

                ThemeOptionSystemDefault(
                    isSelected = currentThemeSetting == ThemeSetting.SYSTEM_DEFAULT,
                    onClick = { onThemeSettingChanged(ThemeSetting.SYSTEM_DEFAULT) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {}
}

@Composable
private fun ThemeOptionSystemDefault(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.BrightnessAuto,
            contentDescription = stringResource(R.string.theme_system_default_description),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

