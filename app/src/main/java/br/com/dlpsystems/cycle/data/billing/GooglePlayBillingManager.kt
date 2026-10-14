package br.com.dlpsystems.cycle.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.domain.model.SubscriptionState
import br.com.dlpsystems.cycle.domain.repository.BillingProductsState
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GooglePlayBillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataSource,
) : BillingRepository, PurchasesUpdatedListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val premium = MutableStateFlow(false)
    private val state = MutableStateFlow(SubscriptionState.FREE)
    private val _productsState = MutableStateFlow(BillingProductsState.LOADING)
    private val details = mutableMapOf<String, ProductDetails>()

    override val isPremiumUser: StateFlow<Boolean> = premium.asStateFlow()
    override val subscriptionState: StateFlow<SubscriptionState> = state.asStateFlow()
    override val productsState: StateFlow<BillingProductsState> = _productsState.asStateFlow()

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    init {
        scope.launch {
            preferences.premiumCached.collect { cached ->
                if (!premium.value) applyPremium(cached)
            }
        }
    }

    override fun connect() {
        if (client.isReady) {
            queryProducts()
            refresh()
            return
        }
        _productsState.value = BillingProductsState.LOADING
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    refresh()
                } else {
                    _productsState.value = BillingProductsState.ERROR
                }
            }

            override fun onBillingServiceDisconnected() {
                _productsState.value = BillingProductsState.ERROR
            }
        })
    }

    override fun refresh() {
        if (!client.isReady) return
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
        ) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryPurchasesAsync
            val activePurchases = purchases.filter { purchase ->
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.products.any { it in BillingProducts.all }
            }
            activePurchases.forEach { acknowledgePurchaseIfNeeded(it) }
            val active = activePurchases.isNotEmpty()
            scope.launch {
                preferences.setPremiumCached(active)
                applyPremium(active)
            }
        }
    }

    override fun purchase(activity: Activity, productId: String) {
        if (!client.isReady) {
            connect()
            return
        }
        val product = details[productId]
        if (product == null) {
            queryProducts()
            return
        }
        val offer = product.subscriptionOfferDetails?.firstOrNull() ?: return
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offer.offerToken)
                        .build(),
                ),
            )
            .build()
        val result = client.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e("BillingManager", "Falha ao iniciar compra: ${result.debugMessage}")
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach { acknowledgePurchaseIfNeeded(it) }
            refresh()
        }
    }

    private fun acknowledgePurchaseIfNeeded(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            client.acknowledgePurchase(params) { /* no-op acknowledgment callback */ }
        }
    }

    private fun queryProducts() {
        if (!client.isReady) return
        _productsState.value = BillingProductsState.LOADING
        val products = BillingProducts.all.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        client.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                queryResult.productDetailsList.forEach { details[it.productId] = it }
                _productsState.value = if (details.isNotEmpty()) BillingProductsState.SUCCESS else BillingProductsState.ERROR
            } else {
                _productsState.value = BillingProductsState.ERROR
            }
        }
    }

    private fun applyPremium(active: Boolean) {
        premium.value = active
        state.value = if (active) SubscriptionState.PREMIUM else SubscriptionState.FREE
    }
}
