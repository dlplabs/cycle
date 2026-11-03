package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.IavpRiskLevel
import br.com.dlpsystems.cycle.domain.model.TransitProtectionSession
import br.com.dlpsystems.cycle.domain.model.TrustedContact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PersonalSafetyUseCasesTest {

    private val calculator = IavpCalculator()

    @Test
    fun trustedContactEncodesAndDecodesProperly() {
        val contact = TrustedContact(
            id = "c1",
            name = "Mãe | Maria",
            phone = "+5511988887777",
            relationship = "Família",
        )
        val encoded = contact.encode()
        val decoded = TrustedContact.decode(encoded)

        assertNotNull(decoded)
        assertEquals("c1", decoded!!.id)
        assertEquals("Mãe   Maria", decoded.name)
        assertEquals("+5511988887777", decoded.phone)
        assertEquals("Família", decoded.relationship)
    }

    @Test
    fun trustedContactDecodesLegacyPhoneOnlyString() {
        val decoded = TrustedContact.decode("11999990000")
        assertNotNull(decoded)
        assertEquals("Contato de Confiança", decoded!!.name)
        assertEquals("11999990000", decoded.phone)
    }

    @Test
    fun transitProtectionSessionTracksRemainingTimeAndExpiration() {
        val start = 1_000_000L
        val durationMin = 30
        val expires = start + (durationMin * 60_000L)

        val session = TransitProtectionSession(
            destinationLabel = "Voltando para Casa",
            durationMinutes = durationMin,
            startedAtMillis = start,
            expiresAtMillis = expires,
        )

        val encoded = session.encode()
        val decoded = TransitProtectionSession.decode(encoded)

        assertNotNull(decoded)
        assertEquals("Voltando para Casa", decoded!!.destinationLabel)
        assertEquals(30, decoded.durationMinutes)

        // 10 minutos depois: restam 20 minutos (1200 segundos)
        val tenMinLater = start + (10 * 60_000L)
        assertEquals(1200L, decoded.remainingSeconds(tenMinLater))
        assertFalse(decoded.isExpired(tenMinLater))

        // 31 minutos depois: expirado (0 segundos restantes)
        val afterExpiry = start + (31 * 60_000L)
        assertEquals(0L, decoded.remainingSeconds(afterExpiry))
        assertTrue(decoded.isExpired(afterExpiry))
        assertNull(TransitProtectionSession.decode(null))
    }

    @Test
    fun iavpCalculatorClassifiesAllRiskLevelsCorrectly() {
        // 1. Risco Baixo (nenhuma resposta positiva)
        val lowResult = calculator.calculate(emptyMap())
        assertEquals(IavpRiskLevel.BAIXO, lowResult.riskLevel)
        assertFalse(lowResult.fatalTriggerActivated)

        // 2. Risco Moderado (2 respostas peso 2 = 4 pontos)
        val modResult = calculator.calculate(mapOf("Q05" to true, "Q06" to true))
        assertEquals(4, modResult.score)
        assertEquals(IavpRiskLevel.MODERADO, modResult.riskLevel)

        // 3. Risco Grave (3 respostas peso 2 = 6 pontos)
        val graveResult = calculator.calculate(mapOf("Q05" to true, "Q06" to true, "Q07" to true))
        assertEquals(6, graveResult.score)
        assertEquals(IavpRiskLevel.GRAVE, graveResult.riskLevel)

        // 4. Risco Extremo por Gatilho Fatal (Q01 = categoria 1)
        val fatalResult = calculator.calculate(mapOf("Q01" to true))
        assertTrue(fatalResult.fatalTriggerActivated)
        assertEquals(IavpRiskLevel.EXTREMO, fatalResult.riskLevel)
        assertTrue(fatalResult.quickActions.any { it.phoneNumber == "190" })
        assertTrue(fatalResult.quickActions.any { it.phoneNumber == "180" })
    }
}
