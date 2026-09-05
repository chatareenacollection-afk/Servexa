package com.example.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.ServexaIndigo
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun ProviderPortfolioScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allVideos by viewModel.workVideos.collectAsState()
    val providerVideos = remember(allVideos, currentUser) {
        allVideos.filter { it.providerId == currentUser?.id }
    }

    var showUploadDialog by remember { mutableStateOf(false) }
    var videoTitle by remember { mutableStateOf("") }
    var videoDesc by remember { mutableStateOf("") }
    var videoCat by remember { mutableStateOf("Electrician") }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Work Portfolio",
                subtitle = "${providerVideos.size} published video reels",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadDialog = true },
                containerColor = ServexaIndigo
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload Video")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (providerVideos.isEmpty()) {
            EmptyStateView(
                title = "No Work Videos",
                subtitle = "Upload project videos to showcase your skills and get 3x more bookings.",
                icon = Icons.Default.VideoCameraBack,
                actionButtonText = "+ Upload Video Reel",
                onActionClick = { showUploadDialog = true },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(providerVideos, key = { it.id }) { video ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(video.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(video.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("❤️ ${video.likesCount} Likes", style = MaterialTheme.typography.labelMedium)
                                Text("💬 ${video.commentsCount} Comments", style = MaterialTheme.typography.labelMedium)
                                Text("👁️ ${video.viewsCount} Views", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload Work Walkthrough", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = videoTitle,
                        onValueChange = { videoTitle = it },
                        label = { Text("Project Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = videoDesc,
                        onValueChange = { videoDesc = it },
                        label = { Text("Walkthrough Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = videoCat,
                        onValueChange = { videoCat = it },
                        label = { Text("Category Tag") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (videoTitle.isNotBlank() && videoDesc.isNotBlank()) {
                            viewModel.uploadWorkVideo(videoTitle, videoDesc, videoCat) {
                                showUploadDialog = false
                                videoTitle = ""
                                videoDesc = ""
                            }
                        }
                    }
                ) {
                    Text("Publish Video")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) { Text("Cancel") }
            }
        )
    }
}
