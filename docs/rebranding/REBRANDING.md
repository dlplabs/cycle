Aja como um Engenheiro Android Sênior especialista em UI/UX, Jetpack Compose e Material Design 3. Nós estamos fazendo um rebranding completo do aplicativo "Cycle" (um app nativo de acompanhamento menstrual e bem-estar). 

O novo design sai de um estilo "ferramenta clínica/corporativa" para uma experiência "Wellness Premium": acolhedora, sofisticada, orgânica e baseada em ciência.

Por favor, gere ou refatore os códigos Kotlin para os seguintes pontos, respeitando a arquitetura limpa:

1. TEMA (COLORS & TYPOGRAPHY)
Implemente o `CycleTheme` usando Material 3.
- Cores Principais: `DeepPlum` (0xFF4A2B4D - cor primária e de títulos), `DeepPlumLight` (0xFF6E4572), `OffWhiteBackground` (0xFFF9F7F6) e `SurfaceCard` (0xFFFFFFFF).
- Cores das Fases do Ciclo: `MenstrualTerracotta` (0xFFD07C70), `FollicularSage` (0xFF8DB094), `OvulatoryPeach` (0xFFF4B886) e `LutealLavender` (0xFFBCA6CE).
- Tipografia (Font Pairing): Configure a tipografia do MaterialTheme para usar "Playfair Display" (Serifada) nos títulos (HeadlineLarge, HeadlineMedium) e "Inter" ou "Outfit" (Sans-serif geométrica) no corpo do texto (BodyLarge, BodyMedium).

2. REFATORAÇÃO DA CYCLEWHEEL (CANVAS E ANIMAÇÃO)
Atualmente, a roda do ciclo (CycleWheel) parece um gráfico de pizza tradicional. 
- Reescreva o componente `@Composable CycleWheel`.
- Use `Canvas` para desenhar os arcos do ciclo. Em vez de blocos duros, desenhe `Stroke` (linhas espessas) com `StrokeCap.Round` (pontas arredondadas) para dar um aspecto contínuo e orgânico.
- Adicione uma animação de entrada fluida usando `animateFloatAsState` (ex: os arcos preenchendo a tela do zero até o valor atual ao abrir o app).
- O centro do círculo deve exibir o dia atual e a fase usando a tipografia Serifada (Playfair Display) na cor `DeepPlum`.

3. CARDS DE INTERFACE E ÍCONES (CORREÇÃO DE GLYPHS)
- Crie um componente de base `@Composable CycleCard` que tenha o fundo branco, cantos arredondados (16.dp) e uma sombra super suave/difusa (use `shadowElevation = 2.dp` ou `Modifier.shadow` com uma cor ambiente bem clara) para destacar do fundo Off-White.
- Nossos ícones antigos estavam quebrando (Tofu boxes). Refatore os ícones das telas para usar a biblioteca `Icons.Rounded` ou `Icons.Outlined` do Material (se necessário, inclua a dependência `material-icons-extended` no gradle). Eles combinam melhor com a estética premium.

4. GLANCE APPWIDGET (TELA INICIAL)
Refatore o nosso widget feito com Jetpack Glance.
- O Widget deve ter o fundo branco (`ColorProvider(Color.White)`) com cantos arredondados.
- Exiba o círculo do ciclo (uma versão simplificada da CycleWheel usando formas ou imagens compatíveis com Glance).
- Aplique as novas cores de fase (Terracota, Sálvia, Pêssego, Lavanda) dependendo do estado atual do usuário (buscado do banco local/DataStore).
- Como o Glance tem limitações com fontes customizadas, use a fonte nativa do sistema em negrito, mas pinte o texto principal com a nossa cor `DeepPlum` para manter a identidade de marca fora do app.

Por favor, me entregue o código estruturado, começando pelos arquivos de `Color.kt`, `Type.kt` e `Theme.kt`, e depois os componentes visuais.