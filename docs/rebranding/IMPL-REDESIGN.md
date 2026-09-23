Aja como um Engenheiro Android Sênior, especialista em UI/UX, Jetpack Compose e Material Design 3. 

Nós estamos implementando um rebranding completo do aplicativo "Cycle" (um app nativo de acompanhamento menstrual e bem-estar em Kotlin). O novo design evolui de um estilo "ferramenta clínica" para uma experiência "Wellness Premium": acolhedora, sofisticada, orgânica e focada no conforto e privacidade da usuária.

Abaixo, forneço a base de código completa da nova Interface de Usuário (UI). 
Sua tarefa é:
1. Absorver esses códigos e integrá-los à arquitetura do nosso projeto (Clean Architecture + MVVM).
2. Ajustar os `imports` e pacotes para refletir a estrutura local do projeto.
3. Garantir que as dependências necessárias estejam no `build.gradle` (como suporte a Haptic Feedback e Material 3).
4. Me orientar caso eu precise baixar as fontes "Playfair Display" e "Inter" para a pasta `res/font`.

Por favor, analise e integre os seguintes arquivos:

### 1. ui/theme/Color.kt
```kotlin
package com.seuapp.cycle.ui.theme

import androidx.compose.ui.graphics.Color

val DeepPlum = Color(0xFF4A2B4D)
val DeepPlumLight = Color(0xFF6E4572)
val OffWhiteBackground = Color(0xFFF9F7F6)
val SurfaceCard = Color(0xFFFFFFFF)

val MenstrualTerracotta = Color(0xFFD07C70)
val FollicularSage = Color(0xFF8DB094)
val OvulatoryPeach = Color(0xFFF4B886)
val LutealLavender = Color(0xFFBCA6CE)

val TextPrimary = Color(0xFF2C2C2C)
val TextSecondary = Color(0xFF707070)
```

### 2. ui/theme/Type.kt
```kotlin
package com.seuapp.cycle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.seuapp.cycle.R

val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium)
)

val CycleTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = DeepPlum
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = DeepPlum
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary
    )
)
```

### 3. ui/theme/Theme.kt
```kotlin
package com.seuapp.cycle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CycleLightColorScheme = lightColorScheme(
    primary = DeepPlum,
    onPrimary = Color.White,
    secondary = LutealLavender,
    background = OffWhiteBackground,
    surface = SurfaceCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun CycleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CycleLightColorScheme,
        typography = CycleTypography,
        content = content
    )
}
```

### 4. ui/components/CycleWheelOrganic.kt
```kotlin
package com.seuapp.cycle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seuapp.cycle.ui.theme.*

@Composable
fun CycleWheelOrganic(currentDay: Int, currentPhase: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(280.dp).padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            
            drawArc(color = MenstrualTerracotta, startAngle = -90f, sweepAngle = 45f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            drawArc(color = FollicularSage, startAngle = -35f, sweepAngle = 100f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            drawArc(color = OvulatoryPeach, startAngle = 75f, sweepAngle = 45f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            drawArc(color = LutealLavender, startAngle = 130f, sweepAngle = 130f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$currentDay", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 56.sp, color = DeepPlum)
            Text(text = currentPhase, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = DeepPlumLight)
        }
    }
}
```

### 5. ui/screens/DashboardScreen.kt
```kotlin
package com.seuapp.cycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seuapp.cycle.ui.theme.*
import com.seuapp.cycle.ui.components.CycleWheelOrganic

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(OffWhiteBackground).verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text(text = "Olá, Maria", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = DeepPlum, modifier = Modifier.padding(bottom = 24.dp, top = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            CycleWheelOrganic(currentDay = 14, currentPhase = "Fase Lútea")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Cuidados para hoje", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = DeepPlum, modifier = Modifier.padding(bottom = 16.dp))
        
        WellnessCard(title = "Nutrição", description = "Consuma alimentos ricos em magnésio e chá de camomila para reduzir a retenção.", accentColor = FollicularSage)
        WellnessCard(title = "Pele", description = "Foco em hidratação. A variação hormonal pode aumentar a oleosidade folicular.", accentColor = OvulatoryPeach)
    }
}

@Composable
fun WellnessCard(title: String, description: String, accentColor: androidx.compose.ui.graphics.Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), ambientColor = DeepPlumLight, spotColor = DeepPlumLight)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.width(4.dp).height(48.dp).background(accentColor, RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepPlum)
                Text(text = description, fontFamily = Inter, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
```

### 6. ui/screens/SymptomDiaryScreen.kt
```kotlin
package com.seuapp.cycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seuapp.cycle.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomDiaryScreen() {
    var selectedFlow by remember { mutableStateOf("Moderado") }
    val flowOptions = listOf("Leve", "Moderado", "Intenso", "Sem fluxo")
    
    var selectedMoods by remember { mutableStateOf(setOf("Calma")) }
    val moodOptions = listOf("Calma", "Energia", "Cansaço", "Ansiedade")

    Column(
        modifier = Modifier.fillMaxSize().background(OffWhiteBackground).verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text(text = "Como você está hoje?", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = DeepPlum, modifier = Modifier.padding(bottom = 24.dp))

        SectionTitle("Fluxo Menstrual")
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            flowOptions.forEach { option ->
                val isSelected = selectedFlow == option
                WellnessChip(text = option, isSelected = isSelected, selectedColor = MenstrualTerracotta) { selectedFlow = option }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Humor")
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            moodOptions.forEach { option ->
                val isSelected = selectedMoods.contains(option)
                WellnessChip(text = option, isSelected = isSelected, selectedColor = LutealLavender) { 
                    selectedMoods = if (isSelected) selectedMoods - option else selectedMoods + option 
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { /* ViewModel Call */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
            Text(text = "Salvar Registro", fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
fun WellnessChip(text: String, isSelected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) selectedColor else SurfaceCard
    val textColor = if (isSelected) Color.White else TextPrimary
    val borderColor = if (isSelected) Color.Transparent else Color.LightGray

    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(backgroundColor).border(1.dp, borderColor, RoundedCornerShape(20.dp)).clickable { onClick() }.padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text = text, fontFamily = Inter, fontSize = 14.sp, color = textColor)
    }
}
```

### 7. ui/screens/SosReliefScreen.kt
```kotlin
package com.seuapp.cycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.seuapp.cycle.ui.theme.*

@Composable
fun SosReliefScreen() {
    val haptic = LocalHapticFeedback.current
    var isBreathing by remember { mutableStateOf(false) }
    var breatheText by remember { mutableStateOf("Iniciar") }

    Column(
        modifier = Modifier.fillMaxSize().background(OffWhiteBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Alívio Rápido", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = DeepPlum, modifier = Modifier.align(Alignment.Start))
        Text(text = "Ferramentas práticas para momentos de dor", fontFamily = Inter, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.align(Alignment.Start).padding(top = 8.dp, bottom = 32.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MenstrualTerracotta.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Bolsa de Calor", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MenstrualTerracotta)
                Text(text = "Apoie uma bolsa morna no abdômen por 15 min.", fontFamily = Inter, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
                Button(onClick = { /* Timer */ }, colors = ButtonDefaults.buttonColors(containerColor = MenstrualTerracotta)) {
                    Text("Iniciar 15:00", color = Color.White)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = "Respiração 4-7-8", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepPlum)
                Text(text = "Com pulsos vibratórios para guiar você.", fontFamily = Inter, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp, bottom = 32.dp))

                Box(
                    modifier = Modifier.size(160.dp).clip(CircleShape).background(LutealLavender.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = breatheText, fontFamily = PlayfairDisplay, fontSize = 24.sp, color = DeepPlum, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = { isBreathing = !isBreathing }, colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)) {
                    Text(if (isBreathing) "Parar" else "Iniciar Respiração", color = Color.White)
                }
            }
        }
    }

    LaunchedEffect(isBreathing) {
        while (isBreathing) {
            breatheText = "Inspire..."
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(4000L)
            
            breatheText = "Segure..."
            delay(7000L)
            
            breatheText = "Expire..."
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(8000L)
        }
        if (!isBreathing) breatheText = "Iniciar"
    }
}
```

Após analisar, por favor, gere a implementação adaptando os imports ao projeto atual e prepare as viewmodels se necessário.