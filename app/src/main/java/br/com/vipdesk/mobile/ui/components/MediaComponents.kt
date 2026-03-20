package br.com.vipdesk.mobile.ui.components

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.vipdesk.mobile.ui.theme.*
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

// ============ IMAGE MESSAGE ============

@Composable
fun InlineImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    var showFullScreen by remember { mutableStateOf(false) }

    AsyncImage(
        model = imageUrl,
        contentDescription = "Imagem",
        modifier = modifier
            .widthIn(max = 250.dp)
            .heightIn(max = 300.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { showFullScreen = true },
        contentScale = ContentScale.Fit
    )

    if (showFullScreen) {
        FullScreenImageDialog(
            imageUrl = imageUrl,
            onDismiss = { showFullScreen = false }
        )
    }
}

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagem ampliada",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fechar",
                    tint = Color.White
                )
            }
        }
    }
}

// ============ AUDIO PLAYER ============

@Composable
fun AudioPlayer(
    audioUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    DisposableEffect(audioUrl) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Update progress
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    currentPosition = mp.currentPosition.toFloat()
                }
            }
            delay(200)
        }
    }

    fun initPlayer() {
        if (mediaPlayer != null) return
        isLoading = true
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioUrl)
            setOnPreparedListener { mp ->
                duration = mp.duration.toFloat()
                isPrepared = true
                isLoading = false
                mp.start()
                isPlaying = true
            }
            setOnCompletionListener {
                isPlaying = false
                currentPosition = 0f
            }
            setOnErrorListener { _, _, _ ->
                isLoading = false
                isPrepared = false
                true
            }
            prepareAsync()
        }
    }

    fun togglePlayPause() {
        if (!isPrepared) {
            initPlayer()
            return
        }
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                isPlaying = false
            } else {
                mp.start()
                isPlaying = true
            }
        }
    }

    fun changeSpeed(speed: Float) {
        playbackSpeed = speed
        mediaPlayer?.let { mp ->
            if (isPrepared) {
                mp.playbackParams = mp.playbackParams.setSpeed(speed)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button
        IconButton(
            onClick = { togglePlayPause() },
            modifier = Modifier.size(36.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = VipDeskPurple
                )
            } else {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                    tint = VipDeskPurple,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Progress bar
        Slider(
            value = if (duration > 0) currentPosition / duration else 0f,
            onValueChange = { fraction ->
                val newPos = (fraction * duration).toInt()
                currentPosition = newPos.toFloat()
                mediaPlayer?.seekTo(newPos)
            },
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = VipDeskPurple,
                activeTrackColor = VipDeskPurple,
                inactiveTrackColor = CardBorder
            )
        )

        // Time display
        Text(
            text = formatDuration(currentPosition.toLong()) + "/" + formatDuration(duration.toLong()),
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Speed button
        var showSpeedMenu by remember { mutableStateOf(false) }
        Box {
            TextButton(
                onClick = { showSpeedMenu = true },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            ) {
                Text(
                    text = "${playbackSpeed}x",
                    fontSize = 11.sp,
                    color = VipDeskPurple,
                    fontWeight = FontWeight.Bold
                )
            }
            DropdownMenu(
                expanded = showSpeedMenu,
                onDismissRequest = { showSpeedMenu = false }
            ) {
                listOf(1f, 1.5f, 2f).forEach { speed ->
                    DropdownMenuItem(
                        text = { Text("${speed}x") },
                        onClick = {
                            changeSpeed(speed)
                            showSpeedMenu = false
                        }
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000) / 60
    return "%d:%02d".format(minutes, seconds)
}

// ============ VIDEO MESSAGE ============

@Composable
fun VideoThumbnail(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .widthIn(max = 250.dp)
            .heightIn(min = 120.dp, max = 200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .clickable {
                // Open in external player
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(videoUrl), "video/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    // Open in browser as fallback
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                    context.startActivity(browserIntent)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Play icon overlay
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Reproduzir video",
                tint = VipDeskPurple,
                modifier = Modifier.size(36.dp)
            )
        }

        // Video label
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Video",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ============ DOCUMENT MESSAGE ============

@Composable
fun DocumentMessage(
    fileName: String?,
    documentUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayName = fileName ?: "Documento"
    val extension = fileName?.substringAfterLast(".", "")?.lowercase() ?: ""

    val (icon, iconColor) = when (extension) {
        "pdf" -> Icons.Default.PictureAsPdf to Color(0xFFE53935)
        "doc", "docx" -> Icons.Default.Description to Color(0xFF1976D2)
        "xls", "xlsx" -> Icons.Default.TableChart to Color(0xFF388E3C)
        "ppt", "pptx" -> Icons.Default.Slideshow to Color(0xFFE65100)
        "mp3", "wav", "ogg" -> Icons.Default.AudioFile to VipDeskPurple
        else -> Icons.Default.InsertDriveFile to TextSecondary
    }

    Surface(
        modifier = modifier
            .widthIn(max = 250.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(documentUrl)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) { }
            },
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5),
        border = ButtonDefaults.outlinedButtonBorder(true)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1C1B1F)
                )
                if (extension.isNotEmpty()) {
                    Text(
                        text = extension.uppercase(),
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                Icons.Default.Download,
                contentDescription = "Baixar",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
