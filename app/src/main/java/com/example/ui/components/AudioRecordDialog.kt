package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.NotesMakerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecordBottomSheet(
    viewModel: NotesMakerViewModel,
    onDismiss: () -> Unit
) {
    val recordingState by viewModel.recordingState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showSaveDialog by remember { mutableStateOf(false) }
    var recordingTitle by remember {
        mutableStateOf("Voice Note ${SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date())}")
    }

    // Audio file picker contract for selecting existing audio files from device
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "Audio_${System.currentTimeMillis()}.m4a"
            viewModel.importAudioFile(it, fileName)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (recordingState.isRecording) {
                viewModel.stopRecording(recordingTitle, discard = true)
            }
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("audio_record_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Record Audio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = {
                        if (recordingState.isRecording) {
                            viewModel.stopRecording(recordingTitle, discard = true)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.testTag("close_record_sheet_btn")
                ) {
                    Text("Close")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (recordingState.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Microphone Notice",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recordingState.error ?: "Microphone error occurred",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.startRecording() },
                    modifier = Modifier.fillMaxWidth().testTag("retry_record_btn")
                ) {
                    Text("Retry Microphone")
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (!recordingState.isRecording) {
                // Idle state: ready to record
                Text(
                    text = "Tap the microphone to start recording your voice note.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.startRecording() },
                    modifier = Modifier
                        .size(80.dp)
                        .testTag("start_recording_btn"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start Recording",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Option to import audio from device storage
                OutlinedButton(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("import_audio_btn")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Audio File from Device")
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Active recording state
                Text(
                    text = if (recordingState.isPaused) "Recording Paused" else "Recording...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (recordingState.isPaused) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                val minutes = recordingState.durationSeconds / 60
                val seconds = recordingState.durationSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = if (recordingState.isPaused) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                AmplitudeWaveform(
                    amplitude = recordingState.currentAmplitude,
                    isPaused = recordingState.isPaused
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Discard Button
                    FilledTonalButton(
                        onClick = {
                            viewModel.stopRecording(recordingTitle, discard = true)
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("discard_recording_btn"),
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Discard Recording"
                        )
                    }

                    // Pause / Resume Button
                    Button(
                        onClick = {
                            if (recordingState.isPaused) {
                                viewModel.resumeRecording()
                            } else {
                                viewModel.pauseRecording()
                            }
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("pause_resume_btn"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (recordingState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (recordingState.isPaused) "Resume" else "Pause",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Stop & Save Button
                    Button(
                        onClick = {
                            showSaveDialog = true
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("save_recording_btn"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Recording"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Recording") },
            text = {
                Column {
                    Text(
                        text = "Enter a title for this voice note:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recordingTitle,
                        onValueChange = { recordingTitle = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recording_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopRecording(recordingTitle, discard = false)
                        showSaveDialog = false
                        onDismiss()
                    },
                    modifier = Modifier.testTag("confirm_save_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AmplitudeWaveform(
    amplitude: Int,
    isPaused: Boolean,
    barCount: Int = 24
) {
    val normalizedAmp = (amplitude.toFloat() / 32767f).coerceIn(0.08f, 1.0f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val factor = kotlin.math.sin((i.toDouble() / barCount) * Math.PI).toFloat()
            val heightFraction = if (isPaused) 0.12f else (normalizedAmp * factor).coerceIn(0.12f, 1.0f)
            val animatedHeight by animateFloatAsState(
                targetValue = heightFraction,
                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
                label = "bar_height"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((56 * animatedHeight).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPaused) MaterialTheme.colorScheme.outlineVariant
                        else MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}
