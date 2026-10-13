package br.com.dlpsystems.cycle.domain.repository

import android.app.Activity
import br.com.dlpsystems.cycle.domain.model.SubscriptionState
import kotlinx.coroutines.flow.StateFlow

enum class BillingProductsState {
    LOADING,
    SUCCESS,
    ERROR,
}

interface BillingRepository {
    val isPremiumUser: StateFlow<Boolean>
    val subscriptionState: StateFlow<SubscriptionState>
    val productsState: StateFlow<BillingProductsState>
    fun connect()
    fun purchase(activity: Activity, productId: String)
    fun refresh()
}
