package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.core.designsystem.CycleCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.domain.model.PillarGuidance

@Composable
fun PhaseRecommendationCard(
    title: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    guidance: PillarGuidance,
    onOpenSource: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CycleCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(HeaderSage.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (iconRes != null) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = title,
                            modifier = Modifier.size(72.dp),
                            tint = Color.Unspecified,
                        )
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(40.dp),
                            tint = DeepPlum,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepPlum,
                    )
                    Text(
                        text = guidance.guidance,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            guidance.citation?.let { citation ->
                ScientificSourceBadge(citation = citation, onOpen = onOpenSource)
            }
        }
    }
}
