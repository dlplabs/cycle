package br.com.dlpsystems.cycle.domain.repository

import br.com.dlpsystems.cycle.domain.model.SubscriptionState
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isPremiumUser: StateFlow<Boolean>
    val subscriptionState: StateFlow<SubscriptionState>
    fun connect()
    fun purchase(activity: android.app.Activity, productId: String)
    fun refresh()
}
