package br.com.dlpsystems.cycle.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.accessibility.accessibleTouchTarget
import br.com.dlpsystems.cycle.core.config.AppConfig
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.CycleTheme
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import br.com.dlpsystems.cycle.domain.model.CyclePhase

@Composable
fun LoginScreen(
    onRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmail,
        onPasswordChange = viewModel::onPassword,
        onSignIn = viewModel::signIn,
        onGoogleSignIn = { viewModel.signInWithGoogle(context) },
        onRegister = onRegister,
    )
}

@Composable
fun LoginContent(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onRegister: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    CycleTheme(phase = CyclePhase.LUTEAL) {
        AuthColumn {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_moldura_botanica),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                )
                Text(
                    text = AppConfig.displayName,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            if (!state.backendAvailable) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.backend_missing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.sign_in),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    PasswordField(
                        value = state.password,
                        visible = passwordVisible,
                        onVisibleChange = { passwordVisible = it },
                        onValueChange = onPasswordChange,
                        isError = state.error is AuthFailure.WeakPassword,
                        onDone = {
                            focusManager.clearFocus()
                            if (!state.loading && state.backendAvailable) onSignIn()
                        },
                    )

                    AuthErrorText(state.error, state.birthDateInvalid)

                    PrimaryButton(
                        text = stringResource(R.string.sign_in),
                        onClick = {
                            focusManager.clearFocus()
                            onSignIn()
                        },
                        enabled = !state.loading && state.backendAvailable,
                    )

                    OrDivider()

                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        enabled = !state.loading && state.backendAvailable,
                        modifier = Modifier
                            .fillMaxWidth()
                            .accessibleTouchTarget()
                            .heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_google),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(
                                text = stringResource(R.string.sign_in_google),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.register_prompt),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.register_prompt_detail),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            PrimaryButton(
                text = stringResource(R.string.go_to_register),
                onClick = onRegister,
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RegisterContent(
        state = state,
        onNameChange = viewModel::onName,
        onBirthDateChange = viewModel::onBirthDate,
        onEmailChange = viewModel::onEmail,
        onPasswordChange = viewModel::onPassword,
        onRegister = viewModel::register,
        onBack = onBack,
    )
}

@Composable
fun RegisterContent(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    CycleTheme(phase = CyclePhase.LUTEAL) {
        AuthColumn {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_moldura_botanica),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                )
                Text(
                    text = stringResource(R.string.create_account),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )

                    OutlinedTextField(
                        value = state.birthDate,
                        onValueChange = onBirthDateChange,
                        label = { Text(stringResource(R.string.birth_date)) },
                        placeholder = { Text(stringResource(R.string.birth_date_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        isError = state.birthDateInvalid,
                        supportingText = {
                            Text(
                                text = if (state.birthDateInvalid) {
                                    stringResource(R.string.error_birth_date)
                                } else {
                                    stringResource(R.string.birth_date_hint)
                                },
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    PasswordField(
                        value = state.password,
                        visible = passwordVisible,
                        onVisibleChange = { passwordVisible = it },
                        onValueChange = onPasswordChange,
                        isError = state.error is AuthFailure.WeakPassword,
                        onDone = {
                            focusManager.clearFocus()
                            if (!state.loading && state.name.isNotBlank() && state.backendAvailable) {
                                onRegister()
                            }
                        },
                    )

                    AuthErrorText(state.error, state.birthDateInvalid)

                    PrimaryButton(
                        text = stringResource(R.string.create_account),
                        onClick = {
                            focusManager.clearFocus()
                            onRegister()
                        },
                        enabled = !state.loading && state.name.isNotBlank() && state.backendAvailable,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .accessibleTouchTarget(),
            ) {
                Text(
                    text = stringResource(R.string.go_to_login),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun AuthColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = { content() },
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.password)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        isError = isError,
        supportingText = { Text(stringResource(R.string.password_hint)) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { onVisibleChange(!visible) }) {
                Text(if (visible) "Ocultar" else "Mostrar")
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
    )
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "ou",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AuthErrorText(error: AuthFailure?, birthDateInvalid: Boolean) {
    val message = when {
        birthDateInvalid -> stringResource(R.string.error_birth_date)
        error is AuthFailure.InvalidCredentials -> stringResource(R.string.error_invalid_credentials)
        error is AuthFailure.EmailInUse -> stringResource(R.string.error_email_in_use)
        error is AuthFailure.WeakPassword -> stringResource(R.string.error_weak_password)
        error is AuthFailure.NotConfigured -> stringResource(R.string.error_not_configured)
        error is AuthFailure.GoogleNotConfigured -> stringResource(R.string.error_google_not_configured)
        error is AuthFailure.ProviderDisabled -> stringResource(R.string.error_provider_disabled)
        error is AuthFailure.Network -> stringResource(R.string.error_network)
        error is AuthFailure.Unknown -> stringResource(R.string.error_unknown)
        else -> null
    }
    if (message != null) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginContent(
        state = AuthUiState(backendAvailable = true),
        onEmailChange = {},
        onPasswordChange = {},
        onSignIn = {},
        onGoogleSignIn = {},
        onRegister = {},
    )
}
