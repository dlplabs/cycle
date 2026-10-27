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
     - Quando atingir 365 dias (12 meses) sem nenhum sangramento menstrual, o aplicativo emite um marco comemorativo e acolhedor de consolidação da menopausa, desativando predições de fertilidade e convertendo o painel para **Modo Longevidade Saudável**.

3. **Guardião da Saúde Óssea e Força Muscular**:
   - Metas personalizadas baseadas em diretrizes da NOF (National Osteoporosis Foundation):
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
   - Conteúdos educativos baseados em evidência sobre lubrificantes íntimos (isotônicos, sem glicerina e com pH compatível de 3,8 a 4,5), hidratantes vaginais de ácido hialurônico para uso contínuo e terapias locais de estrogênio prescritas por ginecologistas.

## Consequências

### Positivas
- Transforma o Cycle em um parceiro vitalício da mulher, quebrando o abandono que usuárias maduras sofrem na maioria dos aplicativos de saúde menstrual.
- Foco preventivo comprovado contra as duas maiores causas de morbimortalidade na mulher idosa: fraturas osteoporóticas e eventos cardiovasculares.
- Recuperação da autoestima, conforto íntimo e saúde sexual na maturidade.

### Negativas / Mitigações
- Necessidade de exames laboratoriais complementares. *Mitigação:* O app atua como repositório e organizador pessoal de saúde, sem pretender substituir o médico assistente.

## Testes e Critérios de Aceite
- Teste unitário para `ConfirmMenopauseUseCaseTest` verificando contagem de 365 dias de amenorreia.
- Teste unitário para `ClassifyBoneDensityUseCaseTest` baseado nos valores de T-score.
- Teste do temporizador de Kegel garantindo ciclo de vibração correto de contração e relaxamento.
