package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.PlayfairDisplay

@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    onAccount: (() -> Unit)? = null,
    titleModifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .heightIn(min = 48.dp),
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = DeepPlum,
                )
            }
        }

        Text(
            text = title,
            modifier = titleModifier
                .align(Alignment.CenterStart)
                .padding(
                    start = if (onBack != null) 48.dp else 12.dp,
                    end = if (onAccount != null) 48.dp else 12.dp,
                ),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.SemiBold,
                color = DeepPlum,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (onAccount != null) {
            IconButton(
                onClick = onAccount,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.nav_account),
                    tint = DeepPlum,
                )
            }
        }
    }
}
