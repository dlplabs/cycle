package br.com.dlpsystems.cycle.data.billing

import android.app.Activity
import android.content.Context
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.domain.model.SubscriptionState
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
    private val details = mutableMapOf<String, ProductDetails>()

    override val isPremiumUser: StateFlow<Boolean> = premium.asStateFlow()
    override val subscriptionState: StateFlow<SubscriptionState> = state.asStateFlow()

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
            refresh()
            return
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    refresh()
                }
            }

            override fun onBillingServiceDisconnected() = Unit
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
            android.widget.Toast.makeText(context, "Conectando ao Google Play. Aguarde um instante.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val product = details[productId]
        if (product == null) {
            queryProducts()
            android.widget.Toast.makeText(context, "Carregando informacoes do plano. Tente em instantes.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val offer = product.subscriptionOfferDetails?.firstOrNull()
        if (offer == null) {
            android.widget.Toast.makeText(context, "Plano temporariamente indisponivel no Google Play.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
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
            android.util.Log.e("BillingManager", "Falha ao iniciar compra: ${result.debugMessage}")
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
        val products = BillingProducts.all.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        client.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                list.forEach { details[it.productId] = it }
            }
        }
    }

    private fun applyPremium(active: Boolean) {
        premium.value = active
        state.value = if (active) SubscriptionState.PREMIUM else SubscriptionState.FREE
    }
}
