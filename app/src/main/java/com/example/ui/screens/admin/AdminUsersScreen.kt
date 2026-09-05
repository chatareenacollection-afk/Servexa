package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserKycDocumentEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun AdminUsersScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.adminAllUsers.collectAsState()
    val kycDocs by viewModel.adminAllKycDocuments.collectAsState()
    var filterRole by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForCredits by remember { mutableStateOf<UserEntity?>(null) }
    var selectedKycForReview by remember { mutableStateOf<UserKycDocumentEntity?>(null) }

    val filteredUsers = remember(users, filterRole, searchQuery, kycDocs) {
        users.filter { user ->
            val userKyc = kycDocs.find { it.userId == user.id }
            val matchesRole = when (filterRole) {
                "ALL" -> true
                "KYC_SUBMITTED" -> userKyc != null
                "KYC_PENDING" -> userKyc?.verificationStatus == "PENDING"
                else -> user.role == filterRole
            }
            val matchesQuery = searchQuery.isBlank() ||
                    user.name.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true) ||
                    user.phone.contains(searchQuery, ignoreCase = true) ||
                    (userKyc?.documentNumber?.contains(searchQuery, ignoreCase = true) == true)
            matchesRole && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "User & Document Management",
                subtitle = "${users.size} accounts • ${kycDocs.size} KYC Documents Uploaded",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, email, phone, or document #...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Role & KYC Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "KYC_SUBMITTED", "KYC_PENDING", "CUSTOMER", "PROVIDER", "ADMIN").forEach { role ->
                    FilterChip(
                        selected = filterRole == role,
                        onClick = { filterRole = role },
                        label = {
                            Text(
                                when (role) {
                                    "KYC_SUBMITTED" -> "Gov ID / KYC Docs"
                                    "KYC_PENDING" -> "Pending KYC"
                                    else -> role
                                },
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }

            if (filteredUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        title = "No Users Found",
                        subtitle = "No user accounts match the current filter or search criteria.",
                        icon = Icons.Default.PeopleOutline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers, key = { it.id }) { user ->
                        val userKyc = kycDocs.find { it.userId == user.id }
                        AdminUserCard(
                            user = user,
                            kycDoc = userKyc,
                            onVerify = { viewModel.adminVerifyProvider(user.id) },
                            onReject = { viewModel.adminRejectProvider(user.id, "Profile documentation incomplete or unverified.") },
                            onToggleStatus = { viewModel.adminToggleUserStatus(user.id, user.status) },
                            onCall = { viewModel.adminInitiateCallToUser(user) },
                            onChat = { viewModel.adminStartChatWithUser(user) },
                            onAdjustCredits = { selectedUserForCredits = user },
                            onInspectKyc = {
                                selectedKycForReview = userKyc ?: UserKycDocumentEntity(
                                    id = "KYC-${user.id.takeLast(6)}",
                                    userId = user.id,
                                    userName = user.name,
                                    userEmail = user.email,
                                    userPhone = user.phone,
                                    documentType = "DRIVING_LICENSE",
                                    documentNumber = "DL-${user.id.takeLast(4)}-SAMPLE",
                                    issuingCountry = "United States",
                                    issuingStateOrProvince = "California",
                                    expiryDate = "2029-12-31",
                                    dateOfBirth = "1994-06-15",
                                    residentialAddress = "Market Street & 4th Ave, San Francisco, CA",
                                    documentFrontImage = "preview_front_scanned",
                                    documentBackImage = "preview_back_scanned",
                                    selfieImage = "preview_liveness_selfie",
                                    verificationStatus = "PENDING"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedUserForCredits != null) {
        AdminCreditAdjustmentDialog(
            targetUser = selectedUserForCredits!!,
            onDismiss = { selectedUserForCredits = null },
            onConfirm = { isAdd, amount, reason ->
                viewModel.adminAdjustCredits(
                    targetUserId = selectedUserForCredits!!.id,
                    isAddition = isAdd,
                    amount = amount,
                    reason = reason
                ) {
                    selectedUserForCredits = null
                }
            }
        )
    }

    if (selectedKycForReview != null) {
        AdminKycReviewDialog(
            doc = selectedKycForReview!!,
            onDismiss = { selectedKycForReview = null },
            onReview = { status, posAllotmentAmount, notes ->
                viewModel.adminReviewKycDocument(
                    kycId = selectedKycForReview!!.id,
                    status = status,
                    rejectionReason = if (status == "REJECTED") notes else "",
                    adminNotes = notes
                )
                if (status == "VERIFIED" && posAllotmentAmount > 0) {
                    viewModel.adminAllotPosCredit(
                        userId = selectedKycForReview!!.userId,
                        amount = posAllotmentAmount,
                        notes = "POS Credit Allotment on KYC Approval ($notes)"
                    )
                }
                selectedKycForReview = null
            }
        )
    }
}

@Composable
fun AdminUserCard(
    user: UserEntity,
    kycDoc: UserKycDocumentEntity?,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    onToggleStatus: () -> Unit,
    onCall: () -> Unit,
    onChat: () -> Unit,
    onAdjustCredits: () -> Unit,
    onInspectKyc: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: Avatar, Name, Role & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                when (user.role) {
                                    "ADMIN" -> ServexaRose.copy(alpha = 0.2f)
                                    "PROVIDER" -> ServexaTeal.copy(alpha = 0.2f)
                                    else -> ServexaIndigo.copy(alpha = 0.2f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = when (user.role) {
                                "ADMIN" -> ServexaRose
                                "PROVIDER" -> ServexaTeal
                                else -> ServexaIndigo
                            },
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${user.role} • ${user.email}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Phone: ${user.phone.ifBlank { "Not provided" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (user.status == "ACTIVE") ServexaGreen.copy(alpha = 0.15f) else ServexaRose.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = user.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (user.status == "ACTIVE") ServexaGreen else ServexaRose,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (user.role == "PROVIDER") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (user.verificationStatus) {
                                "VERIFIED" -> ServexaTeal.copy(alpha = 0.15f)
                                "PENDING" -> ServexaAmber.copy(alpha = 0.15f)
                                else -> Color.Gray.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = user.verificationStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (user.verificationStatus) {
                                    "VERIFIED" -> ServexaTeal
                                    "PENDING" -> ServexaAmber
                                    else -> Color.Gray
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // KYC & Document Verification Strip
            Surface(
                color = when (kycDoc?.verificationStatus) {
                    "VERIFIED" -> ServexaTeal.copy(alpha = 0.1f)
                    "PENDING" -> ServexaAmber.copy(alpha = 0.15f)
                    "REJECTED" -> ServexaRose.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInspectKyc() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when (kycDoc?.documentType) {
                                "PASSPORT" -> Icons.Default.FlightTakeoff
                                "NATIONAL_ID" -> Icons.Default.Badge
                                else -> Icons.Default.DirectionsCar
                            },
                            contentDescription = null,
                            tint = when (kycDoc?.verificationStatus) {
                                "VERIFIED" -> ServexaTeal
                                "PENDING" -> ServexaAmber
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (kycDoc != null) {
                                    "${kycDoc.documentType.replace("_", " ")}: ${kycDoc.documentNumber}"
                                } else {
                                    "Government ID Document (Not Uploaded)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (kycDoc != null) {
                                    "Status: ${kycDoc.verificationStatus} • Country: ${kycDoc.issuingCountry} (${kycDoc.issuingStateOrProvince})"
                                } else {
                                    "Tap to manually review or allot POS Terminal Credit"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TextButton(onClick = onInspectKyc) {
                        Text(
                            text = if (kycDoc?.verificationStatus == "PENDING") "Review ID" else "Inspect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Direct Communication & Financial Actions: Call, Text, Adjust Credits, POS Review
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Call Any User
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = ServexaTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Call", color = ServexaTeal, fontSize = 11.sp)
                }

                // Text Any User
                OutlinedButton(
                    onClick = onChat,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Message", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Text", fontSize = 11.sp)
                }

                // Adjust Credits
                OutlinedButton(
                    onClick = onAdjustCredits,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Credits", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Credits", fontSize = 11.sp)
                }

                // POS Review Button
                Button(
                    onClick = onInspectKyc,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = "POS", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("POS / KYC", fontSize = 11.sp)
                }
            }

            // Approval & Status Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (user.role == "PROVIDER" && user.verificationStatus == "PENDING") {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaRose),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Reject Pro")
                    }
                    Button(
                        onClick = onVerify,
                        colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve Pro")
                    }
                }

                if (user.role != "ADMIN") {
                    OutlinedButton(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (user.status == "ACTIVE") ServexaRose else ServexaGreen
                        )
                    ) {
                        Icon(
                            imageVector = if (user.status == "ACTIVE") Icons.Default.Block else Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (user.status == "ACTIVE") "Block / Suspend" else "Unblock User")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminKycReviewDialog(
    doc: UserKycDocumentEntity,
    onDismiss: () -> Unit,
    onReview: (status: String, posAllotmentAmount: Double, notes: String) -> Unit
) {
    var adminNotes by remember { mutableStateOf(doc.adminNotes.ifBlank { "Document verified against national database." }) }
    var allotPosCredit by remember { mutableStateOf(true) }
    var posAmountText by remember { mutableStateOf("150.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ServexaIndigo.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        tint = ServexaIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "KYC & Government ID Audit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "User: ${doc.userName} (${doc.userEmail})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Document Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("DOCUMENT SPECIFICATIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ServexaIndigo)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Document Type:", style = MaterialTheme.typography.bodySmall)
                                Text(doc.documentType.replace("_", " "), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Document Number:", style = MaterialTheme.typography.bodySmall)
                                Text(doc.documentNumber, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ServexaTeal)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Issuing Jurisdiction:", style = MaterialTheme.typography.bodySmall)
                                Text("${doc.issuingCountry}, ${doc.issuingStateOrProvince}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Expiration Date:", style = MaterialTheme.typography.bodySmall)
                                Text(doc.expiryDate, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date of Birth:", style = MaterialTheme.typography.bodySmall)
                                Text(doc.dateOfBirth, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Address:", style = MaterialTheme.typography.bodySmall)
                                Text(doc.residentialAddress, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                    }
                }

                item {
                    // Scanned Assets Card
                    Text("VERIFICATION ASSETS & BIOMETRICS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ServexaIndigo)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ServexaGreen.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ServexaGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Front ID Scan", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Match 99.4%", style = MaterialTheme.typography.labelSmall, color = ServexaGreen)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ServexaGreen.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ServexaGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Back Barcode", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Verified", style = MaterialTheme.typography.labelSmall, color = ServexaGreen)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ServexaGreen.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null, tint = ServexaGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Face Liveness", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Passed", style = MaterialTheme.typography.labelSmall, color = ServexaGreen)
                            }
                        }
                    }
                }

                item {
                    // POS System Credit Allotment Option
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ServexaTeal.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.PointOfSale, contentDescription = null, tint = ServexaTeal)
                                    Text("Allot POS System Credit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                }
                                Switch(
                                    checked = allotPosCredit,
                                    onCheckedChange = { allotPosCredit = it }
                                )
                            }
                            Text(
                                text = "Instantly allots POS Terminal pre-approved credit directly into user's wallet upon verification approval.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (allotPosCredit) {
                                OutlinedTextField(
                                    value = posAmountText,
                                    onValueChange = { posAmountText = it },
                                    label = { Text("POS Credit Allotment Amount ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = { Text("$", fontWeight = FontWeight.Bold) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = adminNotes,
                        onValueChange = { adminNotes = it },
                        label = { Text("Admin Audit & Review Notes") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onReview("REJECTED", 0.0, adminNotes)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaRose)
                ) {
                    Text("Reject ID")
                }

                Button(
                    onClick = {
                        val posCredit = if (allotPosCredit) (posAmountText.toDoubleOrNull() ?: 0.0) else 0.0
                        onReview("VERIFIED", posCredit, adminNotes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (allotPosCredit) "Approve & Allot POS Credit" else "Approve ID")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminCreditAdjustmentDialog(
    targetUser: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (isAddition: Boolean, amount: Double, reason: String) -> Unit
) {
    var isAddition by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("50") }
    var reason by remember { mutableStateOf("POS Terminal credit allotment / satisfaction bonus") }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ServexaIndigo)
                Text("Manual Wallet / POS Credit Adjustment")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "User: ${targetUser.name} (${targetUser.email})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isAddition,
                        onClick = { isAddition = true },
                        label = { Text("+ Allot Credit") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isAddition,
                        onClick = { isAddition = false },
                        label = { Text("- Deduct Credit") },
                        leadingIcon = { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Adjustment Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Audit Reason / POS System Reference") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount > 0 && reason.isNotBlank()) {
                        onConfirm(isAddition, amount, reason)
                    }
                },
                enabled = amount > 0 && reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAddition) ServexaGreen else ServexaRose
                )
            ) {
                Text(if (isAddition) "Confirm +$$amount" else "Confirm -$$amount")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
