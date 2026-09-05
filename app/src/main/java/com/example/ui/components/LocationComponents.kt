package com.example.ui.components

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ServexaViewModel
import com.example.util.CapturedLocation
import com.example.util.LocationHelper

/**
 * Universal "Use Current Location" button with built-in runtime permission requesting,
 * GPS acquisition spinner, and immediate coordinates + reverse geocode callback.
 */
@Composable
fun UseCurrentLocationButton(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier,
    buttonText: String = "Use Current Location",
    variant: LocationButtonVariant = LocationButtonVariant.TONAL,
    onLocationCaptured: ((CapturedLocation) -> Unit)? = null
) {
    val context = LocalContext.current
    val isCapturing by viewModel.isCapturingLocation.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted || !LocationHelper.hasLocationPermission(context)) {
            viewModel.captureLiveDeviceLocation(context, onLocationCaptured)
        }
    }

    val handleClick = {
        if (LocationHelper.hasLocationPermission(context)) {
            viewModel.captureLiveDeviceLocation(context, onLocationCaptured)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    when (variant) {
        LocationButtonVariant.TONAL -> {
            FilledTonalButton(
                onClick = handleClick,
                enabled = !isCapturing,
                modifier = modifier.testTag("use_current_location_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = ServexaTeal.copy(alpha = 0.15f),
                    contentColor = ServexaTeal
                )
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = ServexaTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Acquiring GPS...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(buttonText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        LocationButtonVariant.PRIMARY -> {
            Button(
                onClick = handleClick,
                enabled = !isCapturing,
                modifier = modifier.testTag("use_current_location_button_primary"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ServexaTeal,
                    contentColor = Color.White
                )
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Acquiring GPS...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(buttonText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        LocationButtonVariant.OUTLINED -> {
            OutlinedButton(
                onClick = handleClick,
                enabled = !isCapturing,
                modifier = modifier.testTag("use_current_location_button_outlined"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaTeal)
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = ServexaTeal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acquiring GPS...", fontSize = 12.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        LocationButtonVariant.ICON_ONLY -> {
            IconButton(
                onClick = handleClick,
                enabled = !isCapturing,
                modifier = modifier
                    .background(ServexaTeal.copy(alpha = 0.15f), CircleShape)
                    .testTag("use_current_location_icon_btn")
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = ServexaTeal
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Use Current Location",
                        tint = ServexaTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

enum class LocationButtonVariant {
    TONAL, PRIMARY, OUTLINED, ICON_ONLY
}

/**
 * Top Banner / Card for User Account & Profile Screen:
 * Displays real-time exact GPS location, live pulse indicator, exact coordinates,
 * and direct "Use Current Location" and "Add / Edit Location" controls.
 */
@Composable
fun LiveLocationAccountHeaderCard(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier,
    onOpenAddLocationDialog: () -> Unit = {}
) {
    val userLat by viewModel.userLatitude.collectAsState()
    val userLng by viewModel.userLongitude.collectAsState()
    val userLocationName by viewModel.userLocationName.collectAsState()
    val isCapturing by viewModel.isCapturingLocation.collectAsState()
    val isLiveGps by viewModel.isLiveGpsActive.collectAsState()

    // Pulse animation for live GPS fix badge
    val infiniteTransition = rememberInfiniteTransition(label = "gps_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("account_live_location_top_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Status & Badge Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.linearGradient(listOf(ServexaTeal, ServexaIndigo)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "LIVE & EXACT LOCATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = ServexaTeal,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Active Service Address",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Live GPS indicator badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ServexaGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ServexaGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ServexaGreen.copy(alpha = pulseAlpha), CircleShape)
                        )
                        Text(
                            text = if (isCapturing) "ACQUIRING GPS..." else "GPS LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ServexaGreen,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

            // Address Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = userLocationName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Exact GPS: ${"%.5f".format(userLat)}° N, ${"%.5f".format(userLng)}° W (High Precision)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons: "Use Current Location" and "Add / Set Location"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UseCurrentLocationButton(
                    viewModel = viewModel,
                    buttonText = "Use Current Location",
                    variant = LocationButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1.3f)
                )

                OutlinedButton(
                    onClick = onOpenAddLocationDialog,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_set_location_dialog_btn"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditLocationAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set / Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Fast Pre-set Location Chips for Instant Switch
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Quick Location Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        Triple("Downtown SF", 37.7749, -122.4194 to "Market Street & 4th Ave, San Francisco, CA"),
                        Triple("Midtown NY", 40.7580, -73.9855 to "Times Square & 7th Ave, New York, NY"),
                        Triple("Brooklyn", 40.6782, -73.9442 to "Brooklyn Heights, New York, NY"),
                        Triple("Silicon Valley", 37.3861, -122.0839 to "Castro Street, Mountain View, CA"),
                        Triple("Chicago Loop", 41.8781, -87.6298 to "Michigan Ave, Chicago, IL")
                    )

                    items(presets) { (label, lat, data) ->
                        val (lng, addr) = data
                        val isSelected = userLocationName.contains(label.split(" ").first(), ignoreCase = true)

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateUserLocation(lat, lng, addr)
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.testTag("preset_location_${label.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Dialog to Add or Change Custom Address with "Use Current Location" option.
 */
@Composable
fun AddLocationDialog(
    viewModel: ServexaViewModel,
    initialAddress: String = "",
    onDismiss: () -> Unit,
    onLocationSet: (String, Double, Double) -> Unit
) {
    var inputAddress by remember { mutableStateOf(initialAddress.ifBlank { "Market Street & 4th Ave, San Francisco, CA" }) }
    var selectedLat by remember { mutableStateOf(37.7749) }
    var selectedLng by remember { mutableStateOf(-122.4194) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Set / Add Location", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Type an address or tap 'Use Current Location' to capture your live GPS coordinates automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // The prominent "Use Current Location" button
                UseCurrentLocationButton(
                    viewModel = viewModel,
                    buttonText = "Use My Current Live Location",
                    variant = LocationButtonVariant.TONAL,
                    modifier = Modifier.fillMaxWidth(),
                    onLocationCaptured = { loc ->
                        inputAddress = loc.address
                        selectedLat = loc.latitude
                        selectedLng = loc.longitude
                    }
                )

                OutlinedTextField(
                    value = inputAddress,
                    onValueChange = { inputAddress = it },
                    label = { Text("Address / City / Area") },
                    placeholder = { Text("e.g. 742 Evergreen Terrace, Springfield") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_address_input_field"),
                    minLines = 2,
                    maxLines = 3
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = ServexaTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Coordinates: ${"%.4f".format(selectedLat)}, ${"%.4f".format(selectedLng)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputAddress.isNotBlank()) {
                        viewModel.updateUserLocation(selectedLat, selectedLng, inputAddress.trim())
                        onLocationSet(inputAddress.trim(), selectedLat, selectedLng)
                        onDismiss()
                    }
                },
                enabled = inputAddress.isNotBlank(),
                modifier = Modifier.testTag("confirm_set_location_button")
            ) {
                Text("Save Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
