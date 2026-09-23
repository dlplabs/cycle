package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    AndroidView(
        modifier = modifier.fillMaxWidth().height(60.dp),
        factory = { viewContext ->
            AdView(viewContext).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(viewContext, 360),
                )
                adUnitId = viewContext.getString(R.string.admob_banner_id)
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = AdView::destroy,
    )
}
