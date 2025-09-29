package com.example.topseriesapp.ui.configuration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.topseriesapp.R
import com.example.topseriesapp.utils.LocaleHelper

enum class ThemeSetting { LIGHT, DARK, SYSTEM_DEFAULT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    currentThemeSetting: ThemeSetting,
    onThemeSettingChanged: (ThemeSetting) -> Unit,
    onLanguageChanged: () -> Unit, // Parámetro añadido para el callback
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Obtiene el código de idioma actual de la aplicación ("en", "es")
    val currentAppLanguageCode = LocaleHelper.getCurrentAppLocale(context).language

    // Obtiene el nombre a mostrar para el idioma actual usando recursos de string
    val currentLanguageDisplayName = remember(currentAppLanguageCode) {
        when (currentAppLanguageCode) {
            "es" -> context.getString(R.string.language_spanish)
            "en" -> context.getString(R.string.language_english)
            else -> context.getString(R.string.language_english) // Por defecto, inglés
        }
    }

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
                .padding(innerPaddingScaffoldInterno) // Padding de la TopAppBar de esta pantalla
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Para permitir scroll si el contenido crece
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Sección de Tema
            Text(
                text = stringResource(R.string.theme_settings_title), // "Theme Settings" o "Apariencia"
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
                    onClick = { onThemeSettingChanged(ThemeSetting.LIGHT) },
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

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            // Sección de Idioma
            Text(
                text = stringResource(R.string.language_settings_title), // "Language" o "Idioma"
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedButton(
                onClick = { showLanguageDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween, // Para separar el texto y el idioma actual
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.language_settings_title))
                    Text(currentLanguageDisplayName, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentSelectedLanguageCode = currentAppLanguageCode,
            onLanguageSelected = { selectedLanguageCode ->
                // Solo actualiza y recrea si el idioma realmente cambió
                if (currentAppLanguageCode != selectedLanguageCode) {
                    LocaleHelper.setLocale(context, selectedLanguageCode)
                    onLanguageChanged() // Llama al callback para recrear la Activity
                }
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentSelectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = remember {
        listOf(
            // Par: (ID del recurso de string para el nombre a mostrar, código de idioma)
            Pair(R.string.language_english, "en"),
            Pair(R.string.language_spanish, "es")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            Column {
                languages.forEach { (displayNameResId, languageCode) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(languageCode) } // Permite clickear toda la fila
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSelectedLanguageCode == languageCode,
                            onClick = { onLanguageSelected(languageCode) } // Acción al clickear el RadioButton
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(displayNameResId)) // Muestra el nombre del idioma desde strings.xml
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel).uppercase()) // Texto estándar de "Cancelar"
            }
        }
    )
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
