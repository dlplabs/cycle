package br.com.dlpsystems.cycle.presentation.wellness

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.LutealLavender
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.EpdsRiskCategory
import br.com.dlpsystems.cycle.domain.model.LifeStageMode
import br.com.dlpsystems.cycle.domain.usecase.CalculateEpdsScoreUseCase
import br.com.dlpsystems.cycle.domain.usecase.CalculateMrsScoreUseCase
import br.com.dlpsystems.cycle.domain.usecase.EvaluateLamCriteriaUseCase
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import br.com.dlpsystems.cycle.presentation.components.ScientificSourceBadge
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import br.com.dlpsystems.cycle.presentation.settings.SettingsViewModel

data class WellnessFeatureCardData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val cohortBadge: String,
    val onClick: () -> Unit,
)

@Composable
fun WellnessScreen(
    onNavigatePregnancy: () -> Unit,
    onNavigateLunar: () -> Unit,
    onNavigatePill: () -> Unit,
    onNavigateExams: () -> Unit,
    onNavigatePartner: () -> Unit,
    onNavigateBodySymptoms: () -> Unit,
    onNavigateSafety: () -> Unit = {},
    onPaywall: () -> Unit,
    onAccount: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val activeStage by viewModel.activeLifeStageMode.collectAsStateWithLifecycle()

    var showTtcDialog by remember { mutableStateOf(false) }
    var showEpdsDialog by remember { mutableStateOf(false) }
    var showLamDialog by remember { mutableStateOf(false) }
    var showMrsDialog by remember { mutableStateOf(false) }

    // Diálogos de apoio clínico das fases vitais
    if (showTtcDialog) {
        TtcInfoDialog(
            onDismiss = { showTtcDialog = false },
            onActivateTtc = {
                viewModel.setLifeStageMode(LifeStageMode.TRYING_TO_CONCEIVE)
                showTtcDialog = false
            },
        )
    }

    if (showEpdsDialog) {
        EpdsAssessmentDialog(
            onDismiss = { showEpdsDialog = false },
            onActivatePostpartum = {
                viewModel.setLifeStageMode(LifeStageMode.POSTPARTUM)
                showEpdsDialog = false
            },
        )
    }

    if (showLamDialog) {
        LactationLamDialog(
            onDismiss = { showLamDialog = false },
            onActivateLactation = {
                viewModel.setLifeStageMode(LifeStageMode.LACTATION)
                showLamDialog = false
            },
        )
    }

    if (showMrsDialog) {
        ClimactericMrsDialog(
            onDismiss = { showMrsDialog = false },
            onActivateClimacteric = {
                viewModel.setLifeStageMode(LifeStageMode.PERIMENOPAUSE)
                showMrsDialog = false
            },
        )
    }

    // 1. Jornada & Fases da Mulher (ADR-0001 a ADR-0007)
    val lifeStageFeatures = listOf(
        WellnessFeatureCardData(
            title = stringResource(R.string.pregnancy_mode_title),
            description = "Evolução semana a semana",
            icon = Icons.Default.Favorite,
            iconBgColor = Color(0xFFF2D1D1),
            cohortBadge = "Hadlock / Cardiff (N = 68.654)",
            onClick = onNavigatePregnancy,
        ),
        WellnessFeatureCardData(
            title = "Tentante (TTC)",
            description = "Janela fértil e concepção",
            icon = Icons.Default.Spa,
            iconBgColor = Color(0xFFFFE0B2),
            cohortBadge = "Dunson et al. (N = 1.077)",
            onClick = { showTtcDialog = true },
        ),
        WellnessFeatureCardData(
            title = "Pós-Parto & EPDS",
            description = "Involução e saúde mental",
            icon = Icons.Default.ChildCare,
            iconBgColor = Color(0xFFE1BEE7),
            cohortBadge = "Cox / Pelotas (N = 4.453)",
            onClick = { showEpdsDialog = true },
        ),
        WellnessFeatureCardData(
            title = "Amamentação & MELA",
            description = "Leite materno e lactação",
            icon = Icons.Default.WaterDrop,
            iconBgColor = Color(0xFFB3E5FC),
            cohortBadge = "Bellagio / rBLH (N = 4.118)",
            onClick = { showLamDialog = true },
        ),
        WellnessFeatureCardData(
            title = "Climatério & Menopausa",
            description = "Escala MRS e fogachos",
            icon = Icons.Default.WbSunny,
            iconBgColor = Color(0xFFFFCCBC),
            cohortBadge = "STRAW+10 / SWAN (N > 10.000)",
            onClick = { showMrsDialog = true },
        ),
    )

    // 2. Saúde & Monitoramento Diário
    val dailyFeatures = listOf(
        WellnessFeatureCardData(
            title = stringResource(R.string.body_symptoms_title),
            description = "Mapa corporal de sinais",
            icon = Icons.Default.AccessibilityNew,
            iconBgColor = Color(0xFFD6E3D8),
            cohortBadge = "Monitoramento Somático",
            onClick = onNavigateBodySymptoms,
        ),
        WellnessFeatureCardData(
            title = stringResource(R.string.pill_alarm_title),
            description = "Lembrete de medicação",
            icon = Icons.Default.Alarm,
            iconBgColor = Color(0xFFF4D8B8),
            cohortBadge = "Aderência Terapêutica",
            onClick = onNavigatePill,
        ),
        WellnessFeatureCardData(
            title = stringResource(R.string.exams_folder_title),
            description = "Armazenamento seguro",
            icon = Icons.Default.FolderSpecial,
            iconBgColor = HeaderSage,
            cohortBadge = "Privacidade Local LGPD",
            onClick = onNavigateExams,
        ),
    )

    // 3. Conexão & Autocuidado
    val connectionFeatures = listOf(
        WellnessFeatureCardData(
            title = stringResource(R.string.lunar_mandala_title),
            description = "Sincronia e padrões",
            icon = Icons.Default.NightsStay,
            iconBgColor = LutealLavender,
            cohortBadge = "Ritmos Circadiano & Menstrual",
            onClick = onNavigateLunar,
        ),
        WellnessFeatureCardData(
            title = stringResource(R.string.partner_invite_title),
            description = "Compartilhar dados",
            icon = Icons.Default.People,
            iconBgColor = Color(0xFFE2DAEB),
            cohortBadge = "Apoio e Cuidado Mútuo",
            onClick = onNavigatePartner,
        ),
        WellnessFeatureCardData(
            title = stringResource(R.string.safety_routine_title),
            description = "Check-in e proteção",
            icon = Icons.Default.Shield,
            iconBgColor = Color(0xFFE5D5E8),
            cohortBadge = "Protocolo IAVP",
            onClick = onNavigateSafety,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground),
    ) {
        ScreenHeader(
            title = "Recursos & Bem-Estar",
            onAccount = onAccount,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!isPremium) {
                item(span = { GridItemSpan(2) }) {
                    PremiumBannerCard(onPaywall = onPaywall)
                }
            }

            // Bloco 1: Jornada & Fases da Mulher
            item(span = { GridItemSpan(2) }) {
                SectionHeaderItem(
                    title = "Sua Jornada & Fases de Vida",
                    subtitle = "Recursos clínicos específicos para cada momento biológico",
                )
            }
            items(lifeStageFeatures.size) { index ->
                WellnessFeatureGridCard(feature = lifeStageFeatures[index])
            }

            // Bloco 2: Monitoramento Diário
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(6.dp))
                SectionHeaderItem(
                    title = "Saúde & Monitoramento Diário",
                    subtitle = "Acompanhamento corporal, hábitos e exames",
                )
            }
            items(dailyFeatures.size) { index ->
                WellnessFeatureGridCard(feature = dailyFeatures[index])
            }

            // Bloco 3: Conexão & Autocuidado
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(6.dp))
                SectionHeaderItem(
                    title = "Conexão & Autocuidado",
                    subtitle = "Sincronia lunar, proteção e compartilhamento",
                )
            }
            items(connectionFeatures.size) { index ->
                WellnessFeatureGridCard(feature = connectionFeatures[index])
            }

            item(span = { GridItemSpan(2) }) {
                AdBannerContainer(isPremium = isPremium)
            }
        }
    }
}

@Composable
private fun SectionHeaderItem(
    title: String,
    subtitle: String,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepPlum,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PremiumBannerCard(onPaywall: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onPaywall),
        colors = CardDefaults.cardColors(containerColor = DeepPlum),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4B886)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = DeepPlum,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Seja Premium",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "Navegue sem anúncios e exporte relatórios em PDF.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun WellnessFeatureGridCard(feature: WellnessFeatureCardData) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = feature.onClick),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .height(138.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(feature.iconBgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = DeepPlum,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepPlum,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.cohortBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = DeepPlum.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Diálogos de Apoio Clínico com Evidências (N > 1.000)
// -------------------------------------------------------------------------

@Composable
private fun TtcInfoDialog(
    onDismiss: () -> Unit,
    onActivateTtc: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Planejamento Tentante (TTC)") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "A janela de fertilidade compreende os 5 dias anteriores à ovulação e o dia da ovulação, totalizando 6 dias férteis.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "• Probabilidade de Concepção por Relação (Dunson et al., 2002; N = 1.077 casais):\n" +
                        "  - Dia -2 da ovulação: ~24% a 26%\n" +
                        "  - Dia -1 da ovulação: ~25%\n" +
                        "  - Dia da ovulação: ~12%\n" +
                        "• Método Sintotérmico: Regra 3-sobre-6 de temperatura basal e padrão de muco cervical (Sensiplan, N > 1.000).\n" +
                        "• Ácido Fólico: Recomenda-se 400 mcg/dia antes da concepção para prevenção de defeitos do tubo neural (Czeizel et al., N = 4.753).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ScientificSourceBadge(
                    citation = Citation(
                        authors = "Dunson, D. B. et al.",
                        source = "Human Reproduction, 17(5): 1399–1403, 2002",
                        finding = "Estimativas de fecundabilidade diária na janela fértil em coorte de N = 1.077 casais.",
                        url = "https://doi.org/10.1093/humrep/17.5.1399",
                    ),
                    onOpen = {},
                )
            }
        },
        confirmButton = {
            Button(onClick = onActivateTtc) {
                Text("Ativar Modo Tentante")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
    )
}

@Composable
private fun EpdsAssessmentDialog(
    onDismiss: () -> Unit,
    onActivatePostpartum: () -> Unit,
) {
    val context = LocalContext.current
    var answers by remember { mutableStateOf(mapOf(1 to 0, 2 to 0, 10 to 0)) }
    val useCase = remember { CalculateEpdsScoreUseCase() }
    val evaluation = remember(answers) { useCase(answers) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Triagem Pós-Parto (EPDS)") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Escala de Depressão Pós-Parto de Edimburgo (EPDS), validada para a população brasileira na Coorte de Pelotas (Santos et al., 2007; N = 4.453 puérperas).",
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider()

                Text("Como você tem se sentido nos últimos 7 dias?", fontWeight = FontWeight.SemiBold)

                EpdsItemSelector(
                    question = "1. Fui capaz de rir e ver o lado divertido das coisas:",
                    current = answers[1] ?: 0,
                    onSelect = { answers = answers + (1 to it) },
                )

                EpdsItemSelector(
                    question = "2. Olhei para o futuro com esperança:",
                    current = answers[2] ?: 0,
                    onSelect = { answers = answers + (2 to it) },
                )

                EpdsItemSelector(
                    question = "10. O pensamento de me machucar passou pela minha cabeça:",
                    current = answers[10] ?: 0,
                    onSelect = { answers = answers + (10 to it) },
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HeaderSage.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Classificação: ${evaluation.riskCategory.name}",
                            fontWeight = FontWeight.Bold,
                            color = DeepPlum,
                        )
                        Text(
                            text = evaluation.clinicalRecommendation,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (evaluation.hasSelfHarmIdeation || evaluation.riskCategory == EpdsRiskCategory.PROBABLE_MAJOR_DEPRESSION) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:188"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            ) {
                                Text("Ligar para Apoio CVV (188) — Gratuito")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onActivatePostpartum) {
                Text("Ativar Modo Pós-Parto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
    )
}

@Composable
private fun EpdsItemSelector(
    question: String,
    current: Int,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = question, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..3).forEach { score ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (current == score) DeepPlum else Color.LightGray.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(score) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$score",
                            color = if (current == score) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LactationLamDialog(
    onDismiss: () -> Unit,
    onActivateLactation: () -> Unit,
) {
    var babyAgeDays by remember { mutableIntStateOf(90) }
    var inAmenorrhea by remember { mutableStateOf(true) }
    var isExclusive by remember { mutableStateOf(true) }

    val useCase = remember { EvaluateLamCriteriaUseCase() }
    val result = remember(babyAgeDays, inAmenorrhea, isExclusive) {
        useCase(
            babyAgeDays = babyAgeDays,
            hasVaginalBleedingAfterDay56 = !inAmenorrhea,
            isExclusiveBreastfeedingDayAndNight = isExclusive,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Amamentação & Critérios MELA") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Método da Amenorreia Lactacional (MELA): eficácia superior a 98% nos primeiros 6 meses (Bellagio Consensus / Labbok et al., 1997; N = 4.118 lactantes).",
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider()

                Text("• Critério 1: Bebê com menos de 6 meses (idade: ${babyAgeDays / 30} meses)", style = MaterialTheme.typography.bodySmall)
                Text("• Critério 2: Ausência de menstruação após 56 dias de puerpério", style = MaterialTheme.typography.bodySmall)
                Text("• Critério 3: Amamentação exclusiva em livre demanda dia e noite", style = MaterialTheme.typography.bodySmall)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HeaderSage.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (result.isEligibleForLam) "Critérios Atendidos (${result.estimatedEfficacyPercentage}%)" else "Atenção contraceptiva",
                            fontWeight = FontWeight.Bold,
                            color = DeepPlum,
                        )
                        Text(result.clinicalGuidanceNotice, style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider()
                Text("Conservação do Leite (Rede Brasileira de Leite Humano - Fiocruz):", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Text("• Geladeira: consumir em até 12 horas.\n• Freezer/Congelador: conservar por até 15 dias.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onActivateLactation) {
                Text("Ativar Modo Amamentação")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
    )
}

@Composable
private fun ClimactericMrsDialog(
    onDismiss: () -> Unit,
    onActivateClimacteric: () -> Unit,
) {
    var itemScores by remember { mutableStateOf(mapOf(1 to 1, 3 to 1, 4 to 1, 8 to 0)) }
    val useCase = remember { CalculateMrsScoreUseCase() }
    val result = remember(itemScores) { useCase(itemScores) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Climatério & Escala MRS") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Menopause Rating Scale (MRS): instrumento internacional validado para quantificar sintomas climatéricos (Heinemann et al., 2004; N = 11.140 mulheres).",
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider()

                Text("• Subescala Somato-Vegetativa (fogachos, suores, coração, sono): ${result.somatoVegetativeScore} pts", style = MaterialTheme.typography.bodySmall)
                Text("• Subescala Psicológica (humor, ansiedade, cansaço): ${result.psychologicalScore} pts", style = MaterialTheme.typography.bodySmall)
                Text("• Subescala Urogenital (secura, sintomas urinários): ${result.urogenitalScore} pts", style = MaterialTheme.typography.bodySmall)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HeaderSage.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Pontuação Total: ${result.totalScore} — ${result.severity}",
                            fontWeight = FontWeight.Bold,
                            color = DeepPlum,
                        )
                        Text(
                            text = "Estadiamento STRAW+10 (Harlow et al., 2012; N > 10.000): ciclos com variação >= 7 dias marcam a transição precoce (-2); amenorreia >= 60 dias marca a transição tardia (-1).",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onActivateClimacteric) {
                Text("Ativar Modo Climatério")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
    )
}
