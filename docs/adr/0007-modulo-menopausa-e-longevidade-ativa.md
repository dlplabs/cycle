# ADR-0007: Módulo Menopausa, Pós-Menopausa e Longevidade Ativa

## Status
Aprovado

## Data
2026-10-27

## Contexto
A menopausa é um marco biológico retrospectivo definido pela Organização Mundial da Saúde (OMS) como a cessação permanente da menstruação decorrente da perda da atividade folicular ovariana, confirmada após **12 meses consecutivos de amenorreia** sem outra causa patológica ou fisiológica evidente.

Após a consolidação da menopausa, a mulher ingressa na pós-menopausa (estágios +1 e +2 STRAW+10). A privação crônica de estrogênio acarreta alterações metabólicas e estruturais silenciosas que exigem prevenção ativa:
1. **Perda de Massa Óssea (Osteopenia / Osteoporose):** A taxa de reabsorção óssea supera a osteogênese, elevando exponencialmente o risco de fraturas de fragilidade (quadril, vértebras e rádio distal).
2. **Perda da Cardioproteção Endotelial:** O estrogênio favorece a produção endotelial de óxido nítrico e um perfil lipídico antiaterogênico. Com sua queda, as taxas de doença coronariana e hipertensão arterial na mulher se igualam às dos homens.
3. **Síndrome Geniturinária da Menopausa (GSM):** Atrofia do epitélio vaginal, afinamento da mucosa, perda de rugosidades, aumento do pH vaginal (> 5,0), disúria, urgência miccional e prolapsos de órgãos pélvicos decorrentes de fraqueza do assoalho pélvico.

O foco funcional do Cycle nesta fase desloca-se da fertilidade reprodutiva para a **Longevidade Ativa, Vitalidade e Autonomia**.

## Evidências Científicas e Coortes Populacionais (N > 1.000)

1. **Million Women Study (Beral et al., 2003 / 2007 / *The Lancet*, 362: 419-427 e 369: 1703-1710):**
   - **População Estudada:** **N = 1.388.536 mulheres britânicas** com idade a partir de 50 anos acompanhadas longitudinalmente por décadas pelo Cancer Research UK e NHS.
   - **Achado Clínico Central:** Caracterizou a epidemiologia global da saúde na maturidade feminina. Comprovou categoricamente que **qualquer sangramento vaginal que ocorra após a consolidação de 12 meses de amenorreia (sangramento pós-menopausa)** deve ser considerado suspeito até que se descarte hiperplasia atípica ou carcinoma endometrial por ultrassom transvaginal (espessura endometrial > 4mm) ou histeroscopia. Este dado sustenta o alerta vermelho clínico mandatório no aplicativo caso uma usuária em modo Menopausa registre sangramento.
2. **Women's Health Initiative — WHI (Rossouw et al., 2002 / *JAMA*, 288(3): 321-333; Manson et al., 2013 / *JAMA*, 310(13): 1353-1368):**
   - **População Estudada:** **N = 161.808 mulheres pós-menopáusicas** em 40 centros clínicos dos Estados Unidos.
   - **Achado Clínico Central:** Demonstrou que a perda de massa óssea atinge sua taxa máxima de perda nos primeiros 5 anos pós-menopausa (perda de até 3% a 5% ao ano na coluna lombar). Comprovou que a adesão diária a 1.200 mg de Cálcio associado a 800-1.000 UI de Vitamina D combinada com atividade física resistida (musculação) previne a progressão de osteopenia para osteoporose e reduz o risco de fraturas de fêmur.
3. **Revisões Cochrane e Coortes de Reabilitação do Assoalho Pélvico (Dumoulin et al., 2018 / *Cochrane Database Syst Rev*; Bø et al., 1999 / *BMJ*):**
   - **População Estudada:** Ensaios clínicos e coortes somando mais de **N = 2.000 mulheres na pós-menopausa** com incontinência urinária de esforço e mista.
   - **Achado Clínico Central:** Comprovou que o treinamento domiciliar estruturado dos músculos do assoalho pélvico (exercícios de Kegel) com temporização de contração/relaxamento regular produz cura ou melhora expressiva em mais de 70% das mulheres menopáusicas, restabelecendo o suporte do colo vesical e prevenindo prolapsos urogenitais.

## Decisão Arquitetural

1. **Modelagem de Domínio (`br.com.dlpsystems.cycle.domain.model.menopause`)**:
   ```kotlin
   data class MenopauseConfirmation(
       val isConfirmed: Boolean,
       val lastMenstruationDate: LocalDate,
       val consecutiveAmenorrheaMonths: Int,
       val confirmationDate: LocalDate?,
   )

   data class BoneHealthProfile(
       val latestDexaScanDate: LocalDate? = null,
       val spineTScore: Double? = null,    // Normal > -1.0, Osteopenia -1.0 a -2.5, Osteoporose < -2.5
       val femurTScore: Double? = null,
       val dailyCalciumMgTarget: Int = 1200,
       val dailyVitaminDUnitsTarget: Int = 1000,
       val resistanceTrainingSessionsPerWeekTarget: Int = 3,
   )

   data class CardiovascularVitalsLog(
       val date: LocalDate,
       val systolicMmHg: Int,
       val diastolicMmHg: Int,
       val restingHeartRateBpm: Int? = null,
       val fastingGlucoseMgDl: Int? = null,
       val totalCholesterolMgDl: Int? = null,
       val ldlMgDl: Int? = null,
       val hdlMgDl: Int? = null,
   )

   data class PelvicFloorSession(
       val id: String,
       val timestampEpochMs: Long,
       val completedCycles: Int,
       val contractionSeconds: Int,
       val relaxationSeconds: Int,
       val totalDurationMinutes: Int,
   )
   ```

2. **Avaliador da Consolidação da Menopausa (`ConfirmMenopauseUseCase`)**:
   - Analisa o histórico de sangramentos da usuária:
     - Quando atingir 365 dias (12 meses) sem nenhum sangramento menstrual, o aplicativo emite um marco de consolidação da menopausa, desativando predições de fertilidade e convertendo o painel para **Modo Longevidade Saudável**.
   - **Alerta Clínico Mandatório (Million Women Study):** Se após a confirmação da menopausa for registrado qualquer sangramento vaginal de escape, o app emite alerta imediato recomendando consulta com ginecologista para ultrassom transvaginal.

3. **Guardião da Saúde Óssea e Força Muscular**:
   - Metas personalizadas baseadas nas evidências do WHI e NOF:
     - Cálcio: 1200 mg/dia (via alimentação balanceada e suplementação quando prescrita).
     - Vitamina D: 800 a 2000 UI/dia.
   - Incentivo e acompanhamento de treinos de força (musculação/treinamento resistido) e exercícios de impacto osteogênico, essenciais para preservação da densidade mineral óssea.
   - Histórico e interpretação do T-score de Densitometria Óssea (DEXA) para conscientização da paciente.

4. **Guardião Cardiovascular**:
   - Monitor periódico de pressão arterial (classificação segundo a Sociedade Brasileira de Cardiologia: Ótima < 120/80, Normal, Pré-hipertensão, Hipertensão Estágio 1/2).
   - Acompanhamento de exames laboratoriais anuais de colesterol e glicemia de jejum com gráficos de tendência.

5. **Treinador do Assoalho Pélvico (Exercícios de Kegel)**:
   - Funcionalidade com temporizador háptico discreto:
     - Ritmo de contração sustentada (ex.: 5 segundos contraindo o períneo) e relaxamento ativo (5 segundos de repouso).
     - Guiado inteiramente por vibrações suaves do smartphone, permitindo que a usuária pratique os exercícios em qualquer ambiente sem necessidade de olhar para a tela.
     - Contribui para prevenção e tratamento de incontinência urinária de esforço e suporte aos órgãos pélvicos.

6. **Acolhimento da Síndrome Geniturinária e Sexualidade Madura**:
   - Conteúdos educativos baseados em evidência sobre lubrificantes íntimos (isotônicos, sem glicerina e com pH compatível de 3,8 a 4,5), hidratantes vaginais de ácido hialurônico para uso contínuo e orientações de bem-estar sexual.

## Consequências

### Positivas
- Transforma o Cycle em um parceiro vitalício da mulher, quebrando o abandono que usuárias maduras sofrem na maioria dos aplicativos.
- Foco preventivo comprovado por grandes estudos contra as maiores causas de morbimortalidade na mulher madura: fraturas osteoporóticas e eventos cardiovasculares.
- Acolhimento e suporte clínico com alerta de segurança para sangramento pós-menopausa.

### Negativas / Mitigações
- Necessidade de exames complementares externos. *Mitigação:* O app atua como repositório e organizador pessoal de saúde, sem pretender substituir o médico assistente.

## Testes e Critérios de Aceite
- Teste unitário para `ConfirmMenopauseUseCaseTest` verificando contagem de 365 dias de amenorreia.
- Teste de disparo do alerta crítico de sangramento pós-menopausa.
- Teste unitário para `ClassifyBoneDensityUseCaseTest` baseado nos valores de T-score.
- Teste do temporizador de Kegel garantindo ciclo de vibração correto de contração e relaxamento.
