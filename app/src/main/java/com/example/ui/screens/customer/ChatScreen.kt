package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    recipientId: String,
    recipientName: String,
    bookingId: String,
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val messages by viewModel.getChatMessages(recipientId).collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showMediaSheet by remember { mutableStateOf(false) }
    var selectedMediaPreview by remember { mutableStateOf<ChatMessageEntity?>(null) }
    var showPhotoPicker by remember { mutableStateOf(false) }
    var showVideoPicker by remember { mutableStateOf(false) }

    LaunchedEffect(recipientId) {
        viewModel.markChatRead(recipientId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ServexaIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = recipientName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text(
                                text = recipientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(ServexaGreen, CircleShape))
                                Text(
                                    text = "Online • End-to-End Encrypted",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ServexaTeal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share Location Quick Action
                    IconButton(
                        onClick = {
                            viewModel.sendChatLocation(
                                recipientId = recipientId,
                                recipientName = recipientName,
                                lat = 37.7749,
                                lng = -122.4194,
                                address = "Current Live Location (Market St & 4th Ave)",
                                bookingId = bookingId
                            )
                        }
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Share Location", tint = ServexaAmber)
                    }

                    // Direct Voice Call Button
                    IconButton(
                        onClick = {
                            viewModel.initiateCall(
                                bookingId = bookingId.ifBlank { "DIRECT-CALL" },
                                receiverId = recipientId,
                                receiverName = recipientName,
                                receiverRole = if (currentUser?.role == "CUSTOMER") "PROVIDER" else "CUSTOMER"
                            )
                        }
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Voice Call", tint = ServexaTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Quick Action Phrases
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Hello!", "Where are you now?", "Shared location", "Work completed!").forEach { phrase ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.sendChatMessage(
                                        recipientId = recipientId,
                                        recipientName = recipientName,
                                        text = phrase,
                                        bookingId = bookingId
                                    )
                                },
                                label = { Text(phrase, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Input & Attachment Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Media Attach Button (+)
                        IconButton(
                            onClick = { showMediaSheet = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Attach Media",
                                tint = ServexaIndigo,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Photo quick button
                        IconButton(
                            onClick = { showPhotoPicker = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Send Photo",
                                tint = ServexaTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Video quick button
                        IconButton(
                            onClick = { showVideoPicker = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Send Video",
                                tint = ServexaAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Type a message...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ServexaIndigo,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        FilledIconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val textToSend = inputText
                                    inputText = ""
                                    viewModel.sendChatMessage(
                                        recipientId = recipientId,
                                        recipientName = recipientName,
                                        text = textToSend,
                                        bookingId = bookingId
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("chat_send_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = ServexaIndigo
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ServexaIndigo.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = ServexaIndigo,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "Say hello to $recipientName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You can share text, photos, video clips, and real-time live location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderId == currentUser?.id
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val formattedTime = timeFormat.format(Date(msg.timestamp))

                    ChatBubble(
                        msg = msg,
                        isMine = isMine,
                        formattedTime = formattedTime,
                        onMediaClick = { selectedMediaPreview = msg },
                        onLocationClick = {
                            if (bookingId.isNotBlank()) {
                                viewModel.navigateTo(ScreenDestination.ActiveBookingTracking(bookingId))
                            }
                        }
                    )
                }
            }
        }
    }

    // Media Options Sheet
    if (showMediaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMediaSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Share with $recipientName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Send Photo
                    MediaOptionButton(
                        icon = Icons.Default.CameraAlt,
                        label = "Photo / Proof",
                        color = ServexaIndigo,
                        onClick = {
                            showMediaSheet = false
                            showPhotoPicker = true
                        }
                    )

                    // Send Video
                    MediaOptionButton(
                        icon = Icons.Default.Videocam,
                        label = "Video Clip",
                        color = ServexaAmber,
                        onClick = {
                            showMediaSheet = false
                            showVideoPicker = true
                        }
                    )

                    // Send Location
                    MediaOptionButton(
                        icon = Icons.Default.ShareLocation,
                        label = "Live Location",
                        color = ServexaTeal,
                        onClick = {
                            showMediaSheet = false
                            viewModel.sendChatLocation(
                                recipientId = recipientId,
                                recipientName = recipientName,
                                lat = 37.7749,
                                lng = -122.4194,
                                address = "Current GPS Coordinates (San Francisco Bay)",
                                bookingId = bookingId
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Photo Preset / Attachment Selector
    if (showPhotoPicker) {
        AlertDialog(
            onDismissRequest = { showPhotoPicker = false },
            title = { Text("Attach Photo to Chat") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a photo attachment type to send:", style = MaterialTheme.typography.bodySmall)

                    val photos = listOf(
                        "Work In Progress Proof" to "https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=600&q=80",
                        "Completed Service Result" to "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=600&q=80",
                        "Receipt & Invoice Photo" to "https://images.unsplash.com/photo-1554415707-9e49016e3687?auto=format&fit=crop&w=600&q=80",
                        "House Gate / Entry Landmark" to "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=600&q=80"
                    )

                    photos.forEach { (title, url) ->
                        OutlinedButton(
                            onClick = {
                                viewModel.sendChatPhoto(
                                    recipientId = recipientId,
                                    recipientName = recipientName,
                                    photoUrl = url,
                                    caption = title,
                                    bookingId = bookingId
                                )
                                showPhotoPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(18.dp), tint = ServexaIndigo)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(title, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Video Preset / Attachment Selector
    if (showVideoPicker) {
        AlertDialog(
            onDismissRequest = { showVideoPicker = false },
            title = { Text("Attach Video Clip to Chat") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a video clip to send:", style = MaterialTheme.typography.bodySmall)

                    val videos = listOf(
                        Triple("Plumbing Diagnostics Video", 15, "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                        Triple("Electrical Wiring Inspection", 20, "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
                        Triple("AC Unit Cooling Test", 12, "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
                    )

                    videos.forEach { (title, duration, url) ->
                        Button(
                            onClick = {
                                viewModel.sendChatVideo(
                                    recipientId = recipientId,
                                    recipientName = recipientName,
                                    videoUrl = url,
                                    caption = title,
                                    durationSeconds = duration,
                                    bookingId = bookingId
                                )
                                showVideoPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaAmber)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$title ($duration s)", fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVideoPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Media Viewer Fullscreen Dialog
    selectedMediaPreview?.let { msg ->
        AlertDialog(
            onDismissRequest = { selectedMediaPreview = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (msg.mediaType == "PHOTO") Icons.Default.Photo else Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = ServexaIndigo
                    )
                    Text(if (msg.mediaType == "PHOTO") "Photo Preview" else "Video Player")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (msg.mediaType == "PHOTO") Icons.Default.Image else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (msg.mediaType == "PHOTO") "HD Media View Active" else "Playing: ${msg.mediaCaption} (${msg.videoDurationSec}s)",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (msg.mediaCaption.isNotBlank()) {
                        Text(
                            text = "Caption: ${msg.mediaCaption}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedMediaPreview = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun MediaOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ChatBubble(
    msg: ChatMessageEntity,
    isMine: Boolean,
    formattedTime: String,
    onMediaClick: () -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) ServexaIndigo else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                // PHOTO MESSAGE RENDER
                if (msg.mediaType == "PHOTO") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .clickable(onClick = onMediaClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                            Text("📷 Photo Attachment", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Tap to view full resolution", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // VIDEO MESSAGE RENDER
                if (msg.mediaType == "VIDEO") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .clickable(onClick = onMediaClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(ServexaAmber, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Text("🎥 Video (${msg.videoDurationSec}s)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Tap to play video clip", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // LOCATION MESSAGE RENDER
                if (msg.mediaType == "LOCATION") {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onLocationClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ServexaTeal, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Live GPS Pin Shared", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(msg.locationAddress.ifBlank { "Tap to view on live map" }, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, maxLines = 2)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // TEXT BODY
                if (msg.text.isNotBlank() && (msg.mediaType == "NONE" || msg.mediaCaption.isNotBlank())) {
                    Text(
                        text = msg.text,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // TIMESTAMP & STATUS
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    if (isMine) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = if (msg.read) ServexaTeal else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
