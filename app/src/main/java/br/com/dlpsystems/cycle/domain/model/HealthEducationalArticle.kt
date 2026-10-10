package br.com.dlpsystems.cycle.domain.model

data class HealthEducationalArticle(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String, // ex: "Desenvolvimento Fetal", "Parto", "Amamentação", "Exercícios"
    val citationAuthors: String,
    val sourceName: String, // ex: "Ministério da Saúde (Brasil)", "OMS (Organização Mundial da Saúde)", "ACOG"
    val publicationDate: String, // ex: "2022 (Diretrizes de Atenção ao Parto)", "2023"
    val referenceUrl: String,
    val keyTakeaways: List<String> = emptyList(),
    val contraindicationsOrAlerts: List<String> = emptyList(),
)
