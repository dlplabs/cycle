package br.com.dlpsystems.cycle.presentation.dashboard

import android.Manifest
import android.app.Activity
import android.graphics.BitmapFactory
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.CycleTheme
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PlayfairDisplay
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.core.designsystem.SurfaceCard
import br.com.dlpsystems.cycle.core.designsystem.TextPrimary
import br.com.dlpsystems.cycle.core.notification.labelRes
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.PhaseStatus
import br.com.dlpsystems.cycle.domain.model.WellnessPillar
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import br.com.dlpsystems.cycle.presentation.components.CoachMarkOverlay
import br.com.dlpsystems.cycle.presentation.components.CycleWheel
import br.com.dlpsystems.cycle.presentation.components.PhaseRecommendationCard
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import br.com.dlpsystems.cycle.presentation.components.coachRoot
import br.com.dlpsystems.cycle.presentation.components.coachTarget
import br.com.dlpsystems.cycle.presentation.components.rememberCoachMark
import br.com.dlpsystems.cycle.presentation.tracking.LogSymptomBottomSheet
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun DashboardScreen(
    startPeriodOnOpen: Boolean,
    onStartPeriodConsumed: () -> Unit,
    onAccount: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLog by remember { mutableStateOf(false) }
    var showCare by remember { mutableStateOf(false) }
    LaunchedEffect(startPeriodOnOpen) {
        if (startPeriodOnOpen) {
            viewModel.startPeriodToday()
            onStartPeriodConsumed()
        }
    }
    val phase = state.result?.phase ?: CyclePhase.LUTEAL
    val result = state.result
    val needsFirstPeriod = result == null || result.status == PhaseStatus.NO_CYCLE
    val coach = rememberCoachMark("today")
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val driveConsent = remember { DriveConsent() }
    val driveConsentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        driveConsent.waiting?.complete(result.resultCode == Activity.RESULT_OK)
    }
    var showPhotoOptions by remember { mutableStateOf(false) }
    fun saveWithDrive(block: (String) -> Unit) {
        val host = activity ?: return
        scope.launch {
            val token = runCatching {
                requestDriveToken(host, driveConsent) { driveConsentLauncher.launch(it) }
            }.getOrNull() ?: return@launch
            block(token)
        }
    }
    LaunchedEffect(state.photoDriveId) {
        val host = activity
        val fileId = state.photoDriveId
        if (host != null && !fileId.isNullOrBlank() && state.avatarBytes == null) {
            val token = runCatching {
                requestDriveToken(host, driveConsent) { driveConsentLauncher.launch(it) }
            }.getOrNull()
            if (token != null) viewModel.loadStoredAvatar(token)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) saveWithDrive { token -> viewModel.uploadAvatar(uri, token) }
    }
    val captureUri = remember {
        val file = File(context.cacheDir, "avatar-capture.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) saveWithDrive { token -> viewModel.uploadAvatar(captureUri, token) }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(captureUri)
    }
    CycleTheme(phase = phase) {
        if (showCare && result?.phase != null) {
            PhaseCareScreen(
                state = state,
                onBack = { showCare = false },
                onAccount = onAccount,
                onOpenSource = viewModel::onOpenSource,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HeaderSage)
                    .coachRoot(coach),
            ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeHeader(
                    name = state.userName,
                    photoUrl = state.photoUrl,
                    photoBytes = state.avatarBytes,
                    onPhotoClick = { showPhotoOptions = true },
                    onAccount = onAccount,
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = OffWhiteBackground,
                ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    val wheel = minOf(maxWidth, maxHeight * 0.96f, 340.dp)
                    AnimatedContent(targetState = result?.phase, label = "phaseCrossfade") { current ->
                        val phaseName = current?.let { stringResource(it.labelRes()) }
                            ?: stringResource(R.string.phase_unknown)
                        CycleWheel(
                            cycleDay = result?.cycleDay ?: 0,
                            cycleLength = result?.cycleLength ?: 28,
                            phase = current,
                            phaseName = phaseName,
                            daysRemaining = result?.daysUntilNextPeriod ?: 0,
                            segments = result?.segments.orEmpty(),
                            wheelSize = wheel,
                        )
                    }
                }
                Text(
                    text = when (result?.status) {
                        PhaseStatus.EXTENDED -> result.message.orEmpty()
                        PhaseStatus.NO_CYCLE, null -> stringResource(R.string.no_cycle_hint)
                        PhaseStatus.IN_PHASE -> stringResource(
                            R.string.days_until_period,
                            result.daysUntilNextPeriod,
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                state.insight?.physiology?.takeIf { it.isNotBlank() && !needsFirstPeriod }?.let { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                PrimaryButton(
                    text = stringResource(
                        if (needsFirstPeriod) R.string.start_first_period else R.string.register_today,
                    ),
                    onClick = {
                        if (needsFirstPeriod) viewModel.startPeriodToday() else showLog = true
                    },
                    modifier = Modifier.coachTarget(coach),
                )
                if (!needsFirstPeriod && result?.phase != null) {
                    TextButton(onClick = { showCare = true }) {
                        Text(stringResource(R.string.see_phase_care))
                    }
                }
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
                }
            }
            CoachMarkOverlay(
                state = coach,
                message = stringResource(
                    if (needsFirstPeriod) R.string.coach_today_start else R.string.coach_today_log,
                ),
            )
            }
        }
        if (showLog) {
            LogSymptomBottomSheet(onDismiss = { showLog = false })
        }
        if (showPhotoOptions) {
            PhotoSourceDialog(
                googleAvailable = !state.googlePhotoUrl.isNullOrBlank(),
                onGoogle = {
                    showPhotoOptions = false
                    saveWithDrive { token -> viewModel.useGooglePhoto(token) }
                },
                onGallery = {
                    showPhotoOptions = false
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onCamera = {
                    showPhotoOptions = false
                    cameraPermission.launch(Manifest.permission.CAMERA)
                },
                onDismiss = { showPhotoOptions = false },
            )
        }
    }
}

@Composable
private fun HomeHeader(
    name: String,
    photoUrl: String?,
    photoBytes: ByteArray?,
    onPhotoClick: () -> Unit,
    onAccount: () -> Unit,
) {
    val firstName = name.trim().substringBefore(' ').ifBlank { name.trim() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (firstName.isBlank()) {
                stringResource(R.string.greeting_anonymous)
            } else {
                stringResource(R.string.greeting, firstName)
            },
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ProfileAvatar(
            name = firstName.ifBlank { name },
            photoUrl = photoUrl,
            photoBytes = photoBytes,
            onClick = onPhotoClick,
        )
        IconButton(onClick = onAccount) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.nav_account),
                tint = TextPrimary,
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    name: String,
    photoUrl: String?,
    photoBytes: ByteArray?,
    onClick: () -> Unit,
) {
    var photo by remember(photoUrl, photoBytes) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(photoUrl, photoBytes) {
        photo = when {
            photoBytes != null -> withContext(Dispatchers.IO) {
                BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)?.asImageBitmap()
            }
            photoUrl != null -> loadProfilePhoto(photoUrl)
            else -> null
        }
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(SurfaceCard)
            .border(2.dp, SurfaceCard, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = photo
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.cd_profile_photo, name),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun PhotoSourceDialog(
    googleAvailable: Boolean,
    onGoogle: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.photo_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.photo_drive_note))
                if (googleAvailable) {
                    TextButton(onClick = onGoogle) {
                        Text(stringResource(R.string.photo_google))
                    }
                }
                TextButton(onClick = onGallery) {
                    Text(stringResource(R.string.photo_gallery))
                }
                TextButton(onClick = onCamera) {
                    Text(stringResource(R.string.photo_camera))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.photo_close))
            }
        },
    )
}

private suspend fun loadProfilePhoto(url: String): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 4_000
        connection.readTimeout = 4_000
        connection.inputStream.use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    }.getOrNull()
}

@Composable
private fun PhaseCareScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onAccount: () -> Unit,
    onOpenSource: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.pillars_title),
            onBack = onBack,
            onAccount = onAccount,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        state.insight?.pillars?.forEach { pillar ->
            PhaseRecommendationCard(
                title = stringResource(pillar.pillar.labelRes()),
                iconRes = pillar.pillar.iconRes(),
                guidance = pillar,
                onOpenSource = { onOpenSource() },
            )
        }
        AdBannerContainer(isPremium = state.premium)
        }
    }
}

private fun WellnessPillar.iconRes(): Int = when (this) {
    WellnessPillar.NUTRITION -> R.drawable.ic_alimentacao
    WellnessPillar.EXERCISE -> R.drawable.ic_yoga
    WellnessPillar.SKIN -> R.drawable.ic_skincare
    WellnessPillar.MIND -> R.drawable.ic_lua_lavanda
}

private fun WellnessPillar.labelRes(): Int = when (this) {
    WellnessPillar.NUTRITION -> R.string.pillar_nutrition
    WellnessPillar.EXERCISE -> R.string.pillar_exercise
    WellnessPillar.SKIN -> R.string.pillar_skin
    WellnessPillar.MIND -> R.string.pillar_mind
}

private class DriveConsent {
    var waiting: CompletableDeferred<Boolean>? = null
}

private suspend fun requestDriveToken(
    activity: Activity,
    consent: DriveConsent,
    launchConsent: (IntentSenderRequest) -> Unit,
): String {
    val client = Identity.getAuthorizationClient(activity)
    val request = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(Scopes.DRIVE_APPFOLDER)))
        .build()
    var result = client.authorize(request).await()
    if (result.hasResolution()) {
        val pending = result.pendingIntent ?: error("drive")
        val deferred = CompletableDeferred<Boolean>()
        consent.waiting = deferred
        launchConsent(IntentSenderRequest.Builder(pending).build())
        if (!deferred.await()) error("drive")
        result = client.authorize(request).await()
    }
    return result.accessToken ?: error("drive")
}
