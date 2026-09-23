package br.com.dlpsystems.cycle.presentation.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import br.com.dlpsystems.cycle.core.accessibility.accessibleTouchTarget
import br.com.dlpsystems.cycle.domain.model.Citation

@Composable
fun ScientificSourceBadge(
    citation: Citation,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(
            text = citation.finding,
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(
            onClick = {
                onOpen(citation.url)
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, citation.url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
            modifier = Modifier.accessibleTouchTarget(),
        ) {
            Text("${citation.authors} — ${citation.source}")
        }
    }
}
