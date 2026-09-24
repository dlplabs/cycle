package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.PlayfairDisplay

@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    titleModifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 24.dp, bottom = 8.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = DeepPlum,
                )
            }
        }
        Text(
            text = title,
            modifier = titleModifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Normal,
                color = DeepPlum,
            ),
        )
    }
}
