package br.com.dlpsystems.cycle.presentation.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BirthDateInputTest {
    @Test
    fun maskInsertsSlashesAndKeepsDigitsOnly() {
        assertEquals("14/04/1982", BirthDateInput.mask("14041982"))
        assertEquals("14/04/1982", BirthDateInput.mask("14/04/1982"))
        assertEquals("14/04", BirthDateInput.mask("14a04"))
        assertEquals("14/04/1982", BirthDateInput.mask("1404198299"))
    }

    @Test
    fun parseAcceptsCompleteBrazilianDate() {
        assertEquals(LocalDate.of(1982, 4, 14), BirthDateInput.parse("14/04/1982"))
        assertNull(BirthDateInput.parse("1982-04-14"))
        assertNull(BirthDateInput.parse("32/13/1982"))
    }
}
