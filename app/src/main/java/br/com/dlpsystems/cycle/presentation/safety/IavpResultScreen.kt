package br.com.dlpsystems.cycle.presentation.safety

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.domain.model.IavpActionType
import br.com.dlpsystems.cycle.domain.model.IavpEvaluationResult
import br.com.dlpsystems.cycle.domain.model.IavpQuickAction
import br.com.dlpsystems.cycle.domain.model.IavpRiskLevel

@Composable
fun IavpResultScreen(
    result: IavpEvaluationResult,
    onCloseAndDestroy: () -> Unit,
) {
    val context = LocalContext.current

    val (badgeBg, badgeText, badgeTitle) = when (result.riskLevel) {
        IavpRiskLevel.EXTREMO -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Risco Extremo Detectado",
        )
        IavpRiskLevel.GRAVE -> Triple(
            Color(0xFFFDE8D7),
            Color(0xFF8A3B14),
            "Risco Elevado",
        )
        IavpRiskLevel.MODERADO -> Triple(
            HeaderSage,
            DeepPlum,
            "Risco Moderado",
        )
        IavpRiskLevel.BAIXO -> Triple(
            Color(0xFFE8F2EA),
            DeepPlum,
            "Risco Baixo / Monitorização",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Nível de Risco e Diagnóstico Humanizado
        AppCard {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = badgeText,
                        )
                        Text(
                            text = badgeTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                        )
                    }
                }

                Text(
                    text = result.summaryTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepPlum,
                )

                Text(
                    text = result.summaryDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Ações Rápidas (190 / 180 / DEAM)
        Text(
            text = "Canais de Apoio Imediato",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepPlum,
        )

        result.quickActions.forEach { action ->
            QuickActionCard(
                action = action,
                onClick = {
                    if (action.phoneNumber != null) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.phoneNumber}"))
                        context.startActivity(intent)
                    } else if (action.actionType == IavpActionType.DEAM_LOCATION) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/delegacia+da+mulher+DEAM"),
                        )
                        context.startActivity(intent)
                    }
                },
            )
        }

        // Recomendações
        AppCard {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Diretrizes de Segurança",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepPlum,
                )
                result.recommendations.forEach { rec ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepPlum,
                        )
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão OBRIGATÓRIO de Destruição do Histórico
        PrimaryButton(
            text = "Concluir e Não Salvar Histórico",
            onClick = onCloseAndDestroy,
        )

        OutlinedButton(
            onClick = onCloseAndDestroy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text("Destruir Histórico e Fechar")
        }
    }
}

@Composable
private fun QuickActionCard(
    action: IavpQuickAction,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(HeaderSage),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (action.phoneNumber != null) Icons.Default.Call else Icons.Default.Security,
                    contentDescription = null,
                    tint = DeepPlum,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepPlum,
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PrimaryButton(
                text = if (action.phoneNumber != null) "Ligar" else "Abrir",
                onClick = onClick,
            )
        }
    }
}
