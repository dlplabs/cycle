package br.com.dlpsystems.cycle.presentation.exams

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.domain.model.ExamCategory
import br.com.dlpsystems.cycle.domain.model.MedicalExam
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamsScreen(
    onBack: () -> Unit,
    viewModel: ExamsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            showImportDialog = true
        }
    }

    val filteredExams = if (state.filterCategory == null) {
        state.exams
    } else {
        state.exams.filter { it.category == state.filterCategory }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderSage)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = DeepPlum)
                }
                Text(
                    text = stringResource(R.string.exams_folder_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePicker.launch(arrayOf("application/pdf", "image/*")) },
                containerColor = DeepPlum,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.exams_import_file))
            }
        },
        containerColor = OffWhiteBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Nota de Privacidade e Segurança
            Card(
                colors = CardDefaults.cardColors(containerColor = HeaderSage.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = DeepPlum)
                    Text(
                        text = stringResource(R.string.exams_privacy_notice),
                        style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum),
                    )
                }
            }

            // Filtros de Categoria
            Text(
                text = "Filtrar por Categoria",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    label = "Todos",
                    selected = state.filterCategory == null,
                    onClick = { viewModel.setFilter(null) },
                )
                ExamCategory.entries.forEach { cat ->
                    FilterChip(
                        label = examCategoryLabel(cat),
                        selected = state.filterCategory == cat,
                        onClick = { viewModel.setFilter(cat) },
                    )
                }
            }

            // Lista de Exames
            if (filteredExams.isEmpty()) {
                Text(
                    text = "Nenhum exame cadastrado nesta categoria. Toque no botão '+' para importar um arquivo seguro.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum.copy(alpha = 0.7f)),
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                filteredExams.forEach { exam ->
                    ExamCard(
                        exam = exam,
                        onOpen = {
                            val file = File(exam.localFilePath)
                            if (file.exists()) {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, exam.mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                runCatching { context.startActivity(intent) }
                            }
                        },
                        onDelete = { viewModel.deleteExam(exam.id) },
                    )
                }
            }
        }
    }

    if (showImportDialog && selectedFileUri != null) {
        ImportExamDialog(
            onDismiss = { showImportDialog = false },
            onConfirm = { title, category, notes ->
                val uri = selectedFileUri!!
                val contentResolver = context.contentResolver
                val stream = contentResolver.openInputStream(uri)
                val type = contentResolver.getType(uri) ?: "application/octet-stream"
                if (stream != null) {
                    viewModel.importExam(
                        title = title,
                        date = LocalDate.now(),
                        category = category,
                        notes = notes,
                        inputStream = stream,
                        fileName = "imported_exam",
                        mimeType = type,
                    )
                }
                showImportDialog = false
            },
        )
    }
}

@Composable
private fun ExamCard(
    exam: MedicalExam,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                )
                Text(
                    text = "${examCategoryLabel(exam.category)} • ${exam.date}",
                    style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
                )
                if (exam.notes.isNotBlank()) {
                    Text(
                        text = exam.notes,
                        style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.85f)),
                    )
                }
            }
            Row {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Default.FileOpen, contentDescription = "Visualizar arquivo", tint = DeepPlum)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = DeepPlum.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) DeepPlum else Color.White,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else DeepPlum,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ImportExamDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ExamCategory, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExamCategory.ULTRASOUND_OBSTETRIC) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dados do Exame") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Exame (ex: Morfológico 1º Tri)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title, selectedCategory, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
            ) {
                Text("Salvar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun examCategoryLabel(category: ExamCategory): String = when (category) {
    ExamCategory.ULTRASOUND_OBSTETRIC -> "Ultrassom"
    ExamCategory.BLOOD_TEST -> "Sangue / Sorologia"
    ExamCategory.URINE_TEST -> "Urina"
    ExamCategory.PAP_SMEAR -> "Papanicolau"
    ExamCategory.MAMMOGRAPHY -> "Mamas"
    ExamCategory.GLUCOSE_TOLERANCE -> "Curva Glicêmica"
    ExamCategory.GENETIC_NIPT -> "Genético / NIPT"
    ExamCategory.OTHER -> "Outros"
}
