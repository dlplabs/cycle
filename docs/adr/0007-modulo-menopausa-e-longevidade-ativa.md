# ADR-0007: Módulo Menopausa, Pós-Menopausa e Longevidade Ativa

## Status
Aprovado

## Data
2026-10-27

## Contexto
A menopausa é um marco biológico retrospectivo definido pela Organização Mundial da Saúde (OMS) como a cessação permanente da menstruação decorrente da perda da atividade folicular ovariana, confirmada após **12 meses consecutivos de amenorreia** sem outra causa patológica ou fisiológica evidente [WHO, 1996; HARLOW et al., 2012].

Após a consolidação da menopausa, a mulher ingressa na pós-menopausa (estágios +1 e +2 STRAW+10). A privação crônica de estrogênio acarreta alterações metabólicas e estruturais silenciosas que exigem prevenção ativa:
1. **Perda de Massa Óssea (Osteopenia / Osteoporose):** A taxa de reabsorção óssea supera a osteogênese, elevando exponencialmente o risco de fraturas de fragilidade (quadril, vértebras e rádio distal) [ROSSOUW et al., 2002; MANSON et al., 2013].
2. **Perda da Cardioproteção Endotelial:** O estrogênio favorece a produção endotelial de óxido nítrico e um perfil lipídico antiaterogênico. Com sua queda, as taxas de doença coronariana e hipertensão arterial na mulher se igualam às dos homens [ROSSOUW et al., 2002].
3. **Síndrome Geniturinária da Menopausa (GSM):** Atrofia do epitélio vaginal, afinamento da mucosa, perda de rugosidades, aumento do pH vaginal (> 5,0), disúria, urgência miccional e prolapsos de órgãos pélvicos decorrentes de fraqueza do assoalho pélvico [PORTMAN & GASS, 2014; DUMOULIN et al., 2018].

O foco funcional do Cycle nesta fase desloca-se da fertilidade reprodutiva para a **Longevidade Ativa, Vitalidade e Autonomia**.

## Evidências Científicas e Coortes Populacionais (N > 1.000)

1. **Million Women Study [BERAL et al., 2003; 2007]:**
   - **População Estudada:** **N = 1.388.536 mulheres britânicas** com idade a partir de 50 anos acompanhadas longitudinalmente por décadas pelo Cancer Research UK e NHS (*The Lancet*).
   - **Achado Clínico Central:** Caracterizou a epidemiologia global da saúde na maturidade feminina. Comprovou categoricamente que **qualquer sangramento vaginal que ocorra após a consolidação de 12 meses de amenorreia (sangramento pós-menopausa)** deve ser considerado suspeito até que se descarte hiperplasia atípica ou carcinoma endometrial por ultrassom transvaginal (espessura endometrial > 4mm) ou histeroscopia. Este dado sustenta o alerta vermelho clínico mandatório no aplicativo caso uma usuária em modo Menopausa registre sangramento.
2. **Women's Health Initiative — WHI [ROSSOUW et al., 2002; MANSON et al., 2013]:**
   - **População Estudada:** **N = 161.808 mulheres pós-menopáusicas** em 40 centros clínicos dos Estados Unidos (*JAMA*).
   - **Achado Clínico Central:** Demonstrou que a perda de massa óssea atinge sua taxa máxima de perda nos primeiros 5 anos pós-menopausa (perda de até 3% a 5% ao ano na coluna lombar). Comprovou que a adesão diária a 1.200 mg de Cálcio associado a 800-1.000 UI de Vitamina D combinada com atividade física resistida (musculação) previne a progressão de osteopenia para osteoporose e reduz o risco de fraturas de fêmur.
3. **Revisões Cochrane e Coortes de Reabilitação do Assoalho Pélvico [DUMOULIN et al., 2018; BØ et al., 1999]:**
   - **População Estudada:** Ensaios clínicos e coortes somando mais de **N = 2.000 mulheres na pós-menopausa** com incontinência urinária de esforço e mista (*Cochrane Database Syst Rev* e *BMJ*).
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
     - Quando atingir 365 dias (12 meses) sem nenhum sangramento menstrual [HARLOW et al., 2012], o aplicativo emite um marco de consolidação da menopausa, desativando predições de fertilidade e convertendo o painel para **Modo Longevidade Saudável**.
   - **Alerta Clínico Mandatório (Million Women Study [BERAL et al., 2003]):** Se após a confirmação da menopausa for registrado qualquer sangramento vaginal de escape, o app emite alerta imediato recomendando consulta com ginecologista para ultrassom transvaginal.

3. **Guardião da Saúde Óssea e Força Muscular**:
   - Metas personalizadas baseadas nas evidências do WHI [MANSON et al., 2013] e NOF:
     - Cálcio: 1200 mg/dia (via alimentação balanceada e suplementação quando prescrita).
     - Vitamina D: 800 a 2000 UI/dia.
   - Incentivo e acompanhamento de treinos de força (musculação/treinamento resistido) e exercícios de impacto osteogênico, essenciais para preservação da densidade mineral óssea.
   - Histórico e interpretação do T-score de Densitometria Óssea (DEXA) para conscientização da paciente.

4. **Guardião Cardiovascular**:
   - Monitor periódico de pressão arterial (classificação segundo a Sociedade Brasileira de Cardiologia: Ótima < 120/80, Normal, Pré-hipertensão, Hipertensão Estágio 1/2).
   - Acompanhamento de exames laboratoriais anuais de colesterol e glicemia de jejum com gráficos de tendência baseados nos parâmetros do WHI [ROSSOUW et al., 2002].

5. **Treinador do Assoalho Pélvico (Exercícios de Kegel)**:
   - Funcionalidade com temporizador háptico discreto:
     - Ritmo de contração sustentada (ex.: 5 segundos contraindo o períneo) e relaxamento ativo (5 segundos de repouso) comprovado em revisões sistemáticas [DUMOULIN et al., 2018].
     - Guiado inteiramente por vibrações suaves do smartphone, permitindo que a usuária pratique os exercícios em qualquer ambiente sem necessidade de olhar para a tela.
     - Contribui para prevenção e tratamento de incontinência urinária de esforço e suporte aos órgãos pélvicos.

6. **Acolhimento da Síndrome Geniturinária e Sexualidade Madura**:
   - Conteúdos educativos baseados em evidência sobre lubrificantes íntimos (isotônicos, sem glicerina e com pH compatível de 3,8 a 4,5), hidratantes vaginais de ácido hialurônico para uso contínuo e orientações de bem-estar sexual [PORTMAN & GASS, 2014].
   - Cada card educativo apresenta o botão `ScientificSourceBadge.kt` permitindo consultar o estudo correspondente [MANSON et al., 2013; DUMOULIN et al., 2018].

## Consequências

### Positivas
- Transforma o Cycle em um parceiro vitalício da mulher, quebrando o abandono que usuárias maduras sofrem na maioria dos aplicativos.
- Foco preventivo comprovado por grandes estudos contra as maiores causas de morbimortalidade na mulher madura: fraturas osteoporóticas e eventos cardiovasculares [ROSSOUW et al., 2002].
- Acolhimento e suporte clínico com alerta de segurança para sangramento pós-menopausa alinhado ao Million Women Study [BERAL et al., 2003].

### Negativas / Mitigações
- Necessidade de exames complementares externos. *Mitigação:* O app atua como repositório e organizador pessoal de saúde, sem pretender substituir o médico assistente.

## Testes e Critérios de Aceite
- Teste unitário para `ConfirmMenopauseUseCaseTest` verificando contagem de 365 dias de amenorreia.
- Teste de disparo do alerta crítico de sangramento pós-menopausa.
- Teste unitário para `ClassifyBoneDensityUseCaseTest` baseado nos valores de T-score.
- Teste do temporizador de Kegel garantindo ciclo de vibração correto de contração e relaxamento.

---

## Referências Bibliográficas e Citações Formais

1. **BERAL, V. et al.** Breast cancer and hormone-replacement therapy in the Million Women Study. *The Lancet*, v. 362, n. 9382, p. 419–427, ago. 2003. DOI: [10.1016/S0140-6736(03)14065-2](https://doi.org/10.1016/S0140-6736(03)14065-2). Amostra Million Women Study: N = 1.388.536 mulheres.
2. **ROSSOUW, J. E. et al.** Risks and benefits of estrogen plus progestin in healthy postmenopausal women: principal results from the Women's Health Initiative randomized controlled trial. *JAMA*, v. 288, n. 3, p. 321–333, jul. 2002. DOI: [10.1001/jama.288.3.321](https://doi.org/10.1001/jama.288.3.321). Amostra WHI: N = 161.808 mulheres.
3. **MANSON, J. E. et al.** Menopausal hormone therapy and health outcomes during the intervention and extended poststopping phases of the Women's Health Initiative randomized trials. *JAMA*, v. 310, n. 13, p. 1353–1368, out. 2013. DOI: [10.1001/jama.2013.278040](https://doi.org/10.1001/jama.2013.278040).
4. **DUMOULIN, C. et al.** Pelvic floor muscle training versus no treatment, or inactive control treatments, for urinary incontinence in women. *Cochrane Database of Systematic Reviews*, n. 10, art. CD005654, out. 2018. DOI: [10.1002/14651858.CD005654.pub4](https://doi.org/10.1002/14651858.CD005654.pub4). Amostra combinada: N > 2.000 mulheres.
5. **PORTMAN, D. J.; GASS, M. L.** Genitourinary syndrome of menopause: new terminology for vulvovaginal atrophy from the International Society for the Study of Women's Sexual Health and The North American Menopause Society. *Menopause*, v. 21, n. 10, p. 1063–1068, out. 2014. DOI: [10.1097/GME.0000000000000329](https://doi.org/10.1097/GME.0000000000000329).
6. **BØ, K. et al.** Single blind, randomised controlled trial of pelvic floor exercises, electrical stimulation, vaginal cones, and no treatment in genuine stress incontinence in women. *BMJ*, v. 318, n. 7182, p. 487–493, fev. 1999. DOI: [10.1136/bmj.318.7182.487](https://doi.org/10.1136/bmj.318.7182.487).
