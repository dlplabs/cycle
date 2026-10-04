package br.com.dlpsystems.cycle.presentation.widget

import androidx.glance.appwidget.SizeMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CycleGlanceWidgetTest {

    @Test
    fun widgetUsesExactSizeModeForDynamicResponsiveScaling() {
        val widget = CycleGlanceWidget()
        assertEquals(SizeMode.Exact, widget.sizeMode)
    }

    @Test
    fun widgetReceiverInstantiatesWidget() {
        val receiver = CycleGlanceWidgetReceiver()
        assertNotNull(receiver.glanceAppWidget)
        assertEquals(SizeMode.Exact, receiver.glanceAppWidget.sizeMode)
    }
}
