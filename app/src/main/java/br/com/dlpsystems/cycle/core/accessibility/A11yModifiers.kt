package br.com.dlpsystems.cycle.core.accessibility

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.accessibleTouchTarget(): Modifier =
    this
        .minimumInteractiveComponentSize()
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
