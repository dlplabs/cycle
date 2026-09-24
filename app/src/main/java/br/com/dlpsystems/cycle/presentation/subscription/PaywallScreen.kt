package br.com.dlpsystems.cycle.presentation.subscription

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.config.AppConfig
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.data.billing.BillingProducts
import br.com.dlpsystems.cycle.data.remote.AnalyticsEvents
import br.com.dlpsystems.cycle.data.remote.AnalyticsService
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {
    val isPremium = billingRepository.isPremiumUser

    init {
        billingRepository.connect()
        analyticsService.log(AnalyticsEvents.EVENT_PAYWALL_VIEWED, screen = "paywall")
        viewModelScope.launch {
            var opened = false
            billingRepository.isPremiumUser.collectLatest { premium ->
                if (opened && premium) {
                    analyticsService.log(AnalyticsEvents.EVENT_SUBSCRIPTION_PURCHASED, screen = "paywall")
                }
                opened = true
            }
        }
    }

    fun buy(activity: Activity, productId: String) {
        billingRepository.purchase(activity, productId)
    }
}

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel(),
) {
    val premium by viewModel.isPremium.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.paywall_title, AppConfig.displayName),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        Text(stringResource(R.string.paywall_body), style = MaterialTheme.typography.bodyLarge)
        if (premium) {
            Text(stringResource(R.string.paywall_active))
        }
        PrimaryButton(
            text = stringResource(R.string.paywall_monthly),
            onClick = { activity?.let { viewModel.buy(it, BillingProducts.MONTHLY) } },
            enabled = !premium,
        )
        PrimaryButton(
            text = stringResource(R.string.paywall_yearly),
            onClick = { activity?.let { viewModel.buy(it, BillingProducts.YEARLY) } },
            enabled = !premium,
        )
        }
    }
}
