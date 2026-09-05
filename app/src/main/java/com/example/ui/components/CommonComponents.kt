package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ProviderSearchResult
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServexaTopBar(
    title: String,
    subtitle: String? = null,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun RatingBadge(rating: Double, reviewCount: Int? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                color = ServexaAmber.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating",
            tint = ServexaAmber,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (MaterialTheme.colorScheme.surface == DarkSurface) TextPrimaryDark else Color(0xFF78350F)
        )
        if (reviewCount != null) {
            Text(
                text = "($reviewCount)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VerificationBadge(isVerified: Boolean = true, modifier: Modifier = Modifier) {
    if (isVerified) {
        Row(
            modifier = modifier
                .background(
                    color = ServexaTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified Professional",
                tint = ServexaTeal,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "VERIFIED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ServexaTeal
            )
        }
    }
}

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status.uppercase()) {
        "REQUESTED" -> Triple(ServexaSky.copy(alpha = 0.15f), ServexaSky, Icons.Default.Schedule)
        "ACCEPTED" -> Triple(ServexaIndigo.copy(alpha = 0.15f), ServexaIndigo, Icons.Default.CheckCircleOutline)
        "PROVIDER_ON_THE_WAY" -> Triple(ServexaAmber.copy(alpha = 0.2f), ServexaAmber, Icons.Default.DirectionsCar)
        "ARRIVED" -> Triple(ServexaTeal.copy(alpha = 0.2f), ServexaTeal, Icons.Default.LocationOn)
        "IN_PROGRESS" -> Triple(ServexaIndigoLight.copy(alpha = 0.2f), ServexaIndigoLight, Icons.Default.Build)
        "COMPLETED" -> Triple(ServexaGreen.copy(alpha = 0.15f), ServexaGreen, Icons.Default.CheckCircle)
        "DISPUTED" -> Triple(ServexaRose.copy(alpha = 0.15f), ServexaRose, Icons.Default.Warning)
        "REFUNDED", "CANCELLED", "REJECTED" -> Triple(Color.Gray.copy(alpha = 0.2f), Color.Gray, Icons.Default.Cancel)
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Info)
    }

    Row(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = status.replace("_", " "),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun CategoryIcon(name: String, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary) {
    val icon = when (name.lowercase()) {
        "restaurant", "food", "dining", "cafe" -> Icons.Default.Restaurant
        "hotel", "hotels", "stay", "resort" -> Icons.Default.Hotel
        "localtaxi", "taxi", "taxis", "cab", "ride" -> Icons.Default.LocalTaxi
        "twowheeler", "bike", "bikes", "motorcycle", "scooter" -> Icons.Default.TwoWheeler
        "storefront", "buysell", "buy-sell", "buy & sell", "marketplace", "shop" -> Icons.Default.Storefront
        "computer", "it", "itspecialist", "it-specialist", "tech", "software" -> Icons.Default.Computer
        "medicalservices", "doctor", "doctors", "medical", "physician", "health" -> Icons.Default.MedicalServices
        "pets", "veterinary", "vet", "animal" -> Icons.Default.Pets
        "bolt", "electrician", "electrical" -> Icons.Default.ElectricBolt
        "acunit", "hvac", "ac" -> Icons.Default.AcUnit
        "carpenter", "carpentry", "handyman" -> Icons.Default.Carpenter
        "plumbing", "plumber", "waterdrop" -> Icons.Default.Plumbing
        "cleaningservices", "cleaning" -> Icons.Default.CleaningServices
        "formatpaint", "painting" -> Icons.Default.FormatPaint
        "homerepairservice", "appliance", "appliances" -> Icons.Default.HomeRepairService
        "carrepair", "directionscar", "automotive", "auto" -> Icons.Default.DirectionsCar
        "yard", "landscaping", "gardening" -> Icons.Default.Spa
        "face", "beauty", "salon", "spa" -> Icons.Default.Face
        "localshipping", "moving", "logistics", "delivery" -> Icons.Default.LocalShipping
        "roofing", "construction" -> Icons.Default.Roofing
        "lock", "locksmith", "security" -> Icons.Default.Lock
        "bugreport", "pestcontrol", "pest" -> Icons.Default.BugReport
        "locallaundryservice", "laundry", "drycleaning" -> Icons.Default.LocalLaundryService
        "school", "tutoring", "education", "classes" -> Icons.Default.School
        "photocamera", "photography", "camera", "video" -> Icons.Default.PhotoCamera
        "celebration", "event", "events", "party" -> Icons.Default.Celebration
        "gavel", "legal", "lawyer", "consulting" -> Icons.Default.Gavel
        "wbsunny", "solar", "energy" -> Icons.Default.WbSunny
        "childcare", "babysitting", "child" -> Icons.Default.ChildCare
        else -> Icons.Default.Build
    }
    Icon(
        imageVector = icon,
        contentDescription = name,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Default.Inbox,
    actionButtonText: String? = null,
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionButtonText != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onActionClick,
                modifier = Modifier.testTag("empty_state_action_button")
            ) {
                Text(actionButtonText)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color = ServexaIndigo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LiveLocationMapCanvas(
    providerName: String,
    status: String,
    distanceKm: Double = 1.6,
    etaMinutes: Int = 5,
    customerName: String = "Customer Destination",
    streetName: String = "Market Street & 4th Ave",
    speedKmh: Double = 32.0,
    isProviderView: Boolean = false,
    onRefreshGps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tealColor = ServexaTeal
    val amberColor = ServexaAmber

    // Infinite animation for pulsing radar and vehicle movement
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Animated vehicle progress along route when on the way
    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicle_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw dark modern map street grid
                val gridColor = Color(0xFF1E293B)
                val highwayColor = Color(0xFF334155)
                val step = 44.dp.toPx()
                for (x in 0..w.toInt() step step.toInt()) {
                    drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), strokeWidth = 2f)
                }
                for (y in 0..h.toInt() step step.toInt()) {
                    drawLine(gridColor, Offset(0f, y.toFloat()), Offset(w, y.toFloat()), strokeWidth = 2f)
                }

                // Stylized Major Cross Avenues (Highways)
                drawLine(highwayColor, Offset(0f, h * 0.45f), Offset(w, h * 0.45f), strokeWidth = 7f)
                drawLine(highwayColor, Offset(w * 0.52f, 0f), Offset(w * 0.52f, h), strokeWidth = 7f)

                // Customer Destination Location (Fixed anchor)
                val customerPos = Offset(w * 0.22f, h * 0.72f)
                // Provider Base Position
                val providerStartPos = Offset(w * 0.82f, h * 0.25f)

                // Live dynamic vehicle position
                val currentVehiclePos = if (status == "PROVIDER_ON_THE_WAY") {
                    val t = vehicleProgress
                    // Cubic Bezier interpolation between provider and customer
                    val p0 = providerStartPos
                    val p1 = Offset(w * 0.6f, h * 0.35f)
                    val p2 = Offset(w * 0.38f, h * 0.8f)
                    val p3 = customerPos
                    val u = 1 - t
                    val tt = t * t
                    val uu = u * u
                    val uuu = uu * u
                    val ttt = tt * t
                    val x = uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x
                    val y = uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y
                    Offset(x, y)
                } else if (status in listOf("ARRIVED", "IN_PROGRESS")) {
                    Offset(customerPos.x + 20f, customerPos.y - 20f)
                } else {
                    providerStartPos
                }

                // Draw Driving Route Polyline
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(providerStartPos.x, providerStartPos.y)
                    cubicTo(
                        w * 0.6f, h * 0.35f,
                        w * 0.38f, h * 0.8f,
                        customerPos.x, customerPos.y
                    )
                }

                // Route Outline Glow
                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.3f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12f)
                )

                // Route Core Line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 14f), 0f)
                    )
                )

                // Customer Destination Pulse & Pin
                drawCircle(color = tealColor.copy(alpha = pulseAlpha), radius = pulseRadius, center = customerPos)
                drawCircle(color = Color.White, radius = 14f, center = customerPos)
                drawCircle(color = tealColor, radius = 10f, center = customerPos)

                // Provider Moving Vehicle Marker & Live Radar Pulse
                drawCircle(color = amberColor.copy(alpha = pulseAlpha), radius = pulseRadius * 1.2f, center = currentVehiclePos)
                drawCircle(color = Color(0xFF0F172A), radius = 18f, center = currentVehiclePos)
                drawCircle(color = amberColor, radius = 14f, center = currentVehiclePos)
                drawCircle(color = Color.White, radius = 6f, center = currentVehiclePos)
            }

            // Top Status & Live Badge Bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ServexaGreen, CircleShape)
                        )
                        Text(
                            text = if (isProviderView) "LIVE TURN-BY-TURN GPS" else "REAL-TIME GPS TRACKING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(14.dp))
                        Text(
                            text = "${speedKmh.toInt()} km/h",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Navigation Instruction Banner
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 46.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isProviderView) Icons.Default.Navigation else Icons.Default.NearMe,
                        contentDescription = null,
                        tint = ServexaTeal,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (status == "PROVIDER_ON_THE_WAY") "En route via $streetName" else "Destination: $customerName",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Bottom ETA & Live Distance Card
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = if (isProviderView) Icons.Default.PersonPinCircle else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (isProviderView) ServexaTeal else ServexaAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isProviderView) "Customer: $customerName" else "Provider: $providerName",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Distance: ${"%.2f".format(distanceKm)} km • Status: ${status.replace("_", " ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    color = when (status) {
                        "ARRIVED" -> ServexaGreen
                        "PROVIDER_ON_THE_WAY" -> ServexaAmber
                        else -> ServexaIndigo
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (status == "ARRIVED") "ARRIVED" else "ETA ~ $etaMinutes MIN",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProviderCardItem(
    item: ProviderSearchResult,
    onViewProfile: () -> Unit,
    onBookNow: () -> Unit,
    onCall: () -> Unit,
    onChat: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onViewProfile),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Brush.linearGradient(listOf(ServexaIndigo, ServexaTeal)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.user.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.user.verificationStatus == "VERIFIED") {
                            VerificationBadge(isVerified = true)
                        }
                    }
                    Text(
                        text = item.profile.title.ifBlank { item.categoryName },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    RatingBadge(rating = item.profile.rating, reviewCount = item.profile.reviewCount)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "From $${"%.0f".format(item.startingPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "📍 ${"%.1f".format(item.estimatedDistanceKm)} km away",
                        style = MaterialTheme.typography.labelSmall,
                        color = ServexaTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onChat != null) {
                    OutlinedButton(
                        onClick = onChat,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat")
                    }
                }

                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaTeal),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call")
                }

                Button(
                    onClick = onBookNow,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Text("Book")
                }
            }
        }
    }
}

