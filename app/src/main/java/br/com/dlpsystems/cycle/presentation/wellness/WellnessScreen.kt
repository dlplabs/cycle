package br.com.dlpsystems.cycle.presentation.wellness

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.LutealLavender
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import br.com.dlpsystems.cycle.presentation.settings.SettingsViewModel

data class WellnessFeatureCardData(
    val titleRes: Int,
    val description: String,
    val icon: ImageVector,
    val iconBgColor: Color,
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

    val features = listOf(
        WellnessFeatureCardData(
            titleRes = R.string.pregnancy_mode_title,
            description = "Evolução semana a semana",
            icon = Icons.Default.Favorite,
            iconBgColor = Color(0xFFF2D1D1),
            onClick = onNavigatePregnancy,
        ),
        WellnessFeatureCardData(
            titleRes = R.string.lunar_mandala_title,
            description = "Sincronia e padrões",
            icon = Icons.Default.NightsStay,
            iconBgColor = LutealLavender,
            onClick = onNavigateLunar,
        ),
        WellnessFeatureCardData(
            titleRes = R.string.pill_alarm_title,
            description = "Lembrete diário",
            icon = Icons.Default.Alarm,
            iconBgColor = Color(0xFFF4D8B8),
            onClick = onNavigatePill,
        ),
        WellnessFeatureCardData(
            titleRes = R.string.exams_folder_title,
            description = "Seus exames seguros",
            icon = Icons.Default.FolderSpecial,
            iconBgColor = HeaderSage,
            onClick = onNavigateExams,
        ),
        WellnessFeatureCardData(
            titleRes = R.string.partner_invite_title,
            description = "Compartilhar dados",
            icon = Icons.Default.People,
            iconBgColor = Color(0xFFE2DAEB),
            onClick = onNavigatePartner,
        ),
        WellnessFeatureCardData(
            titleRes = R.string.body_symptoms_title,
            description = "Mapa corporal",
            icon = Icons.Default.AccessibilityNew,
            iconBgColor = Color(0xFFD6E3D8),
            onClick = onNavigateBodySymptoms,
        ),
        WellnessFeatureCardData(
            titleRes = R.string.safety_routine_title,
            description = "Check-in e proteção",
            icon = Icons.Default.Shield,
            iconBgColor = Color(0xFFE5D5E8),
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

            items(features.size) { index ->
                val feature = features[index]
                WellnessFeatureGridCard(feature = feature)
            }

            item(span = { GridItemSpan(2) }) {
                AdBannerContainer(isPremium = isPremium)
            }
        }
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
                .height(130.dp),
            verticalArrangement = Arrangement.SpaceBetween,
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

            Column {
                Text(
                    text = stringResource(feature.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepPlum,
                    maxLines = 2,
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
            }
        }
    }
}
