package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import br.com.dlpsystems.cycle.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

@Composable
fun AdBannerContainer(
    isPremium: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isPremium) return
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        MobileAds.initialize(context)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 48.dp),
    ) {
        Text(
            text = stringResource(R.string.ad_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val widthDp = maxWidth.value.toInt().coerceAtLeast(1)
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { viewContext ->
                    AdView(viewContext).apply {
                        setAdSize(
                            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(viewContext, widthDp),
                        )
                        adUnitId = viewContext.getString(R.string.admob_banner_id)
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = AdView::destroy,
            )
        }
    }
}
