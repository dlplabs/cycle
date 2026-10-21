package br.com.dlpsystems.cycle.presentation.auth

import android.content.Context
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GoogleIdTokenRequesterTest {
    @Test
    fun nullIntentThrowsGoogleFailed() {
        val context = mockk<Context>(relaxed = true)
        assertThrows(AuthFailure.GoogleFailed::class.java) {
            googleIdTokenFromIntent(context, null)
        }
    }
}
