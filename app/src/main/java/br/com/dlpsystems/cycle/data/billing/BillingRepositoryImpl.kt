package br.com.dlpsystems.cycle.data.billing

import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val manager: GooglePlayBillingManager,
) : BillingRepository by manager
