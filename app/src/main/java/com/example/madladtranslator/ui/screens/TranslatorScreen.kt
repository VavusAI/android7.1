import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
package com.example.madladtranslator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.madladtranslator.model.MadladLanguage
import com.example.madladtranslator.ui.TranslatorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    uiState: TranslatorUiState,
    onRefreshLanguages: () -> Unit,
    onSourceLanguageSelected: (MadladLanguage) -> Unit,
    onTargetLanguageSelected: (MadladLanguage) -> Unit,
    onSwapLanguages: () -> Unit,
    onTextChanged: (String) -> Unit,
    onTranslate: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (uiState.username.isBlank()) "Welcome" else "Welcome ${uiState.username}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Translate effortlessly across the full MADLAD-400 language inventory.",
            style = MaterialTheme.typography.bodyMedium
        )
        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LanguageDropdown(
            label = "Source language",
            languages = uiState.languages,
            selected = uiState.sourceLanguage,
            onSelected = onSourceLanguageSelected,
            enabled = !uiState.isLoading
        )
        LanguageDropdown(
            label = "Target language",
            languages = uiState.languages,
            selected = uiState.targetLanguage,
            onSelected = onTargetLanguageSelected,
            enabled = !uiState.isLoading
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = onSwapLanguages,
                label = { Text("Swap languages") },
                leadingIcon = { Icon(imageVector = Icons.Default.Autorenew, contentDescription = null) }
            )
            AssistChip(onClick = onRefreshLanguages, label = { Text("Refresh languages") })
        }
        OutlinedTextField(
            value = uiState.textToTranslate,
            onValueChange = onTextChanged,
            label = { Text("Text to translate") },
            minLines = 4,
            maxLines = 8,
            enabled = !uiState.isTranslating,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onTranslate() }),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onTranslate,
            enabled = !uiState.isTranslating && uiState.languages.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isTranslating) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Translating…")
            } else {
                Text("Translate")
            }
        }
        if (uiState.translatedText.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Translated text", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = uiState.translatedText, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log out")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    label: String,
    languages: List<MadladLanguage>,
    selected: MadladLanguage?,
    onSelected: (MadladLanguage) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && enabled }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selected?.toString() ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.toString()) },
                    onClick = {
                        onSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}