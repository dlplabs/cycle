package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.config.AppConfig

@Composable
fun HealthDisclaimerDialog(onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.disclaimer_title)) },
        text = { Text(stringResource(R.string.health_disclaimer, AppConfig.displayName)) },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(stringResource(R.string.disclaimer_accept))
            }
        },
    )
}
