package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.domain.model.PillarGuidance

@Composable
fun PhaseRecommendationCard(
    title: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    guidance: PillarGuidance,
    onOpenSource: (String) -> Unit,
    modifier: Modifier = Modifier,
    imageOnRight: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }

    CycleCard(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                onClickLabel = if (expanded) "Recolher detalhes" else "Ver texto completo e sugestões",
            ) {
                expanded = !expanded
            },
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize(),
        ) {
            val imageContent: @Composable () -> Unit = {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(HeaderSage.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (iconRes != null) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = title,
                            modifier = Modifier.size(64.dp),
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
            }

            val textContent: @Composable (Modifier) -> Unit = { textModifier ->
                Column(modifier = textModifier) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepPlum,
                    )
                    Text(
                        text = guidance.guidance,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (imageOnRight) {
                    textContent(Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(14.dp))
                    imageContent()
                } else {
                    imageContent()
                    Spacer(modifier = Modifier.width(14.dp))
                    textContent(Modifier.weight(1f))
                }
            }

            // Dicas / Sugestões adicionais quando expandido
            if (expanded && guidance.suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OffWhiteBackground,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Sugestões práticas para esta fase:",
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepPlum,
                        )
                        guidance.suggestions.forEach { tip ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(DeepPlum),
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DeepPlum.copy(alpha = 0.85f),
                                )
                            }
                        }
                    }
                }
            }

            // Indicador visual de expandir/recolher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = if (expanded) "Toque para recolher" else "Toque para ver mais sugestões",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepPlum.copy(alpha = 0.6f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = DeepPlum.copy(alpha = 0.6f),
                )
            }

            guidance.citation?.let { citation ->
                ScientificSourceBadge(citation = citation, onOpen = onOpenSource)
            }
        }
    }
}
