package br.com.dlpsystems.cycle.presentation.safety

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader

@Composable
fun IavpQuestionnaireScreen(
    onBack: () -> Unit,
    onResultReady: () -> Unit,
    viewModel: IavpViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val questions = viewModel.questions

    if (state.isDisguised) {
        DisguiseHydrationScreen(onExitDisguise = { viewModel.setDisguised(false) })
        return
    }

    if (state.evaluationResult != null) {
        IavpResultScreen(
            result = state.evaluationResult!!,
            onCloseAndDestroy = {
                viewModel.destroySessionAndExit(onBack)
            },
        )
        return
    }

    val currentQ = questions.getOrNull(state.currentQuestionIndex) ?: return
    val progress = (state.currentQuestionIndex + 1).toFloat() / questions.size
    val currentAnswer = state.answersMap[currentQ.id]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = DeepPlum)
            }

            // Botão Discreto de Saída Rápida / Camuflagem
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { viewModel.setDisguised(true) },
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Sair Rápido",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = "Saída Rápida",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Progresso por Etapas
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Etapa ${state.currentQuestionIndex + 1} de ${questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = DeepPlum,
                        )
                        Text(
                            text = "Confidencial",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepPlum,
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = DeepPlum,
                    trackColor = HeaderSage,
                )
            }

            // Pergunta Atual
            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = currentQ.questionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepPlum,
                    )
                    Text(
                        text = currentQ.detailText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botões de Resposta (Sim / Não)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SelectableAnswerCard(
                            text = "Sim",
                            selected = currentAnswer == true,
                            onClick = {
                                viewModel.answerQuestion(currentQ.id, true)
                            },
                            modifier = Modifier.weight(1f),
                        )
                        SelectableAnswerCard(
                            text = "Não",
                            selected = currentAnswer == false,
                            onClick = {
                                viewModel.answerQuestion(currentQ.id, false)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Navegação de Pergunta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (state.currentQuestionIndex > 0) {
                    OutlinedButton(onClick = viewModel::previousQuestion) {
                        Text("Anterior")
                    }
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }

                PrimaryButton(
                    text = if (state.currentQuestionIndex == questions.size - 1) "Ver Diagnóstico" else "Próxima",
                    onClick = {
                        if (currentAnswer == null) {
                            viewModel.answerQuestion(currentQ.id, false)
                        }
                        viewModel.nextQuestion()
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectableAnswerCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) DeepPlum else MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else DeepPlum,
            )
        }
    }
}

@Composable
fun DisguiseHydrationScreen(onExitDisguise: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onExitDisguise),
        color = OffWhiteBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = HeaderSage,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Registro de Hidratação",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Meta diária recomendada nesta fase: 2.2L\nMeta atingida hoje: 1.5L",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
