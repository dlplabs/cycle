package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton

@Composable
fun AuthRequiredDialog(
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    title: String = stringResource(R.string.auth_required_title),
    message: String = stringResource(R.string.auth_required_message),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.auth_required_action_login),
                        onClick = onLogin,
                    )
                    OutlinedButton(
                        onClick = onRegister,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(stringResource(R.string.auth_required_action_register))
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.auth_required_action_not_now))
                    }
                }
            }
        },
        confirmButton = {},
    )
}
