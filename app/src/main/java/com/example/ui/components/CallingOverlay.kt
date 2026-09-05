package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ActiveCallSession
import com.example.ui.theme.*

@Composable
fun CallingOverlay(
    activeCall: ActiveCallSession?,
    callSeconds: Long,
    onAccept: () -> Unit,
    onEnd: () -> Unit
) {
    AnimatedVisibility(
        visible = activeCall != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (activeCall == null) return@AnimatedVisibility

        var isMuted by remember { mutableStateOf(false) }
        var isSpeakerOn by remember { mutableStateOf(false) }

        val formatDuration = remember(callSeconds) {
            val mins = callSeconds / 60
            val secs = callSeconds % 60
            "%02d:%02d".format(mins, secs)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkBg,
                            Color(0xFF0F172A),
                            Color(0xFF020617)
                        )
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Info & Privacy Badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .background(
                                color = ServexaTeal.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = ServexaTealLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "SERVEXA SECURE PBX ENCRYPTED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ServexaTealLight
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(ServexaIndigo, ServexaTeal)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = activeCall.receiverName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${activeCall.receiverRole.replace("_", " ")} • Booking: ${activeCall.bookingId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Call Status or Duration Timer
                    if (activeCall.state == "CONNECTED") {
                        Text(
                            text = formatDuration,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ServexaTealLight
                        )
                    } else {
                        Text(
                            text = "Connecting Secure Voice...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = ServexaAmber
                        )
                    }
                }

                // Middle Call Controls (Mute, Speaker, Keypad)
                if (activeCall.state == "CONNECTED") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute Button
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isMuted) ServexaRose.copy(alpha = 0.3f) else DarkSurfaceCard,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isMuted) ServexaRose else Color.White
                            )
                        }

                        // Speaker Button
                        IconButton(
                            onClick = { isSpeakerOn = !isSpeakerOn },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isSpeakerOn) ServexaTeal.copy(alpha = 0.3f) else DarkSurfaceCard,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) ServexaTealLight else Color.White
                            )
                        }

                        // Dialpad / Note
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(56.dp)
                                .background(DarkSurfaceCard, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dialpad,
                                contentDescription = "Keypad",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Bottom Call Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeCall.state != "CONNECTED" && activeCall.state == "RINGING") {
                        // Accept Call Button
                        FloatingActionButton(
                            onClick = onAccept,
                            containerColor = ServexaGreen,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("accept_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Accept Call",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    // End Call Button
                    FloatingActionButton(
                        onClick = onEnd,
                        containerColor = ServexaRose,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
