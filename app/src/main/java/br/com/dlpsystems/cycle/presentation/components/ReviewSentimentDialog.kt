package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum

private enum class ReviewDialogStep {
    GATE,
    PROMOTER,
    CONSTRUCTIVE,
}

@Composable
fun ReviewSentimentDialog(
    onDismiss: () -> Unit,
    onLater: () -> Unit,
    onRatePlayStore: () -> Unit,
    onShare: () -> Unit,
    onSubmitFeedback: (String) -> Unit,
) {
    var step by remember { mutableStateOf(ReviewDialogStep.GATE) }
    var feedbackText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val titleRes = when (step) {
                ReviewDialogStep.GATE -> R.string.review_prompt_title
                ReviewDialogStep.PROMOTER -> R.string.review_positive_title
                ReviewDialogStep.CONSTRUCTIVE -> R.string.review_feedback_title
            }
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
        },
        text = {
            AnimatedContent(targetState = step, label = "ReviewStepAnimation") { targetStep ->
                when (targetStep) {
                    ReviewDialogStep.GATE -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.review_prompt_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = { step = ReviewDialogStep.PROMOTER },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
                                    shape = MaterialTheme.shapes.large,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(stringResource(R.string.review_option_positive))
                                    }
                                }

                                OutlinedButton(
                                    onClick = { step = ReviewDialogStep.CONSTRUCTIVE },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Feedback,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(stringResource(R.string.review_option_negative))
                                    }
                                }

                                TextButton(
                                    onClick = onLater,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.review_option_later))
                                }
                            }
                        }
                    }

                    ReviewDialogStep.PROMOTER -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.review_positive_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = onRatePlayStore,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
                                    shape = MaterialTheme.shapes.large,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(stringResource(R.string.review_positive_action))
                                    }
                                }

                                OutlinedButton(
                                    onClick = onShare,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(stringResource(R.string.review_share_action))
                                    }
                                }

                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.photo_close))
                                }
                            }
                        }
                    }

                    ReviewDialogStep.CONSTRUCTIVE -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.review_feedback_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it.take(600) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                placeholder = {
                                    Text(stringResource(R.string.review_feedback_hint))
                                },
                                maxLines = 4,
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = { onSubmitFeedback(feedbackText) },
                                    enabled = feedbackText.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
                                    shape = MaterialTheme.shapes.large,
                                ) {
                                    Text(stringResource(R.string.review_feedback_send))
                                }

                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.auth_required_action_not_now))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}
