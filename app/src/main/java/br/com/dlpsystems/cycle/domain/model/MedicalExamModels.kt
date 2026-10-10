package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate

enum class ExamCategory {
    ULTRASOUND_OBSTETRIC,  // Ultrassonografia Obstétrica / Transvaginal
    BLOOD_TEST,            // Hemograma / Sorologias / Hormônios
    URINE_TEST,            // Urina / Urocultura
    PAP_SMEAR,             // Papanicolau (Citopatológico)
    MAMMOGRAPHY,           // Mamografia / Ultrassom das Mamas
    GLUCOSE_TOLERANCE,     // Curva Glicêmica (TOTG)
    GENETIC_NIPT,          // NIPT / Cariótipo / Rastreio Genético
    OTHER,                 // Outros exames
}

data class MedicalExam(
    val id: String,
    val title: String,
    val date: LocalDate,
    val category: ExamCategory,
    val notes: String = "",
    val localFilePath: String, // Caminho local protegido no app storage
    val mimeType: String,
    val fileSizeBytes: Long,
    val isEncrypted: Boolean = true,
)
