package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.entity.PaymentMethodEntity
import com.example.data.local.entity.UserPayoutAccountEntity
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTopUpDialog(
    currentBalance: Double,
    availablePaymentMethods: List<PaymentMethodEntity>,
    onDismiss: () -> Unit,
    onTopUp: (Double, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf("100") }
    var selectedMethod by remember {
        mutableStateOf(availablePaymentMethods.firstOrNull() ?: PaymentMethodEntity(
            id = "default_card",
            name = "Card / Wire",
            type = "CARD",
            accountTitle = "Servexa Gateway",
            accountNumber = "Secure Payment",
            bankOrProviderName = "Stripe Merchant Network"
        ))
    }
    var referenceCode by remember { mutableStateOf("") }
    var showMethodPicker by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val feePercent = selectedMethod.feePercent
    val platformFee = (amount * (feePercent / 100.0) * 100.0).roundToInt() / 100.0
    val netCredit = maxOf(0.0, amount - platformFee)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Top-Up Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Current Balance: $${"%.2f".format(currentBalance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Payment Method Selector
                item {
                    Text(
                        text = "Select Payment Method",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMethodPicker = !showMethodPicker },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedMethod.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${selectedMethod.bankOrProviderName} • ${selectedMethod.accountNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (showMethodPicker) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Select"
                            )
                        }
                    }
                }

                if (showMethodPicker) {
                    items(availablePaymentMethods) { method ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMethod = method
                                    showMethodPicker = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMethod.id == method.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedMethod.id == method.id,
                                    onClick = {
                                        selectedMethod = method
                                        showMethodPicker = false
                                    }
                                )
                                Column {
                                    Text(
                                        text = method.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${method.bankOrProviderName} | Acc: ${method.accountNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (method.instructions.isNotBlank()) {
                                        Text(
                                            text = method.instructions,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Instructions Banner if available
                if (selectedMethod.instructions.isNotBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        text = "Payment Instructions:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = selectedMethod.instructions,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (selectedMethod.routingOrSwift.isNotBlank()) {
                                        Text(
                                            text = selectedMethod.routingOrSwift,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Top-up Amount ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topup_amount_input")
                    )
                }

                // Quick Chip Presets
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("50", "100", "250", "500").forEach { preset ->
                            AssistChip(
                                onClick = { amountText = preset },
                                label = { Text("$$preset") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (amountText == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }

                // Reference / TXID code input
                item {
                    OutlinedTextField(
                        value = referenceCode,
                        onValueChange = { referenceCode = it },
                        label = { Text("Transaction Reference / TXID (Optional)") },
                        placeholder = { Text("e.g. Bank Ref, Card Auth, USDT TXID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fee Breakdown
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deposit Amount:", style = MaterialTheme.typography.bodySmall)
                                Text("$${"%.2f".format(amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Platform Fee (${"%.1f".format(feePercent)}%):", style = MaterialTheme.typography.bodySmall, color = ServexaRose)
                                Text("- $${"%.2f".format(platformFee)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = ServexaRose)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Net Wallet Credit:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("$${"%.2f".format(netCredit)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ServexaGreen)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount > 0) {
                        onTopUp(amount, selectedMethod.name, referenceCode)
                        onDismiss()
                    }
                },
                enabled = amount > 0,
                modifier = Modifier.testTag("confirm_topup_button")
            ) {
                Text("Submit Deposit")
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
fun AttachPayoutAccountDialog(
    existingAccount: UserPayoutAccountEntity?,
    onDismiss: () -> Unit,
    onSave: (accountType: String, holderName: String, bankName: String, accountNumber: String, routingCode: String, swift: String, country: String) -> Unit
) {
    var accountType by remember { mutableStateOf(existingAccount?.accountType ?: "BANK_ACCOUNT") }
    var holderName by remember { mutableStateOf(existingAccount?.accountHolderName ?: "") }
    var bankName by remember { mutableStateOf(existingAccount?.bankOrIssuerName ?: "") }
    var accountNumber by remember { mutableStateOf(existingAccount?.accountOrCardNumber ?: "") }
    var routingCode by remember { mutableStateOf(existingAccount?.routingOrIfscOrCvv ?: "") }
    var swift by remember { mutableStateOf(existingAccount?.swiftOrBic ?: "") }
    var country by remember { mutableStateOf(existingAccount?.country ?: "United States") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (existingAccount != null) "Update Payout Details" else "Attach Payout Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Attach your verified bank account or debit card details. All withdrawals are securely settled to this account within 48 hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Account Type Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = accountType == "BANK_ACCOUNT",
                            onClick = { accountType = "BANK_ACCOUNT" },
                            label = { Text("Bank Account (ACH/Wire)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = accountType == "DEBIT_CARD",
                            onClick = { accountType = "DEBIT_CARD" },
                            label = { Text("Debit / Credit Card") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = holderName,
                        onValueChange = { holderName = it },
                        label = { Text("Account Holder / Legal Name") },
                        placeholder = { Text("e.g. John Doe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("payout_holder_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text(if (accountType == "BANK_ACCOUNT") "Bank Name" else "Card Issuer Bank") },
                        placeholder = { Text("e.g. Chase, Wells Fargo, Citi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("payout_bank_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text(if (accountType == "BANK_ACCOUNT") "Account Number / IBAN" else "16-Digit Card Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("payout_account_number")
                    )
                }

                item {
                    OutlinedTextField(
                        value = routingCode,
                        onValueChange = { routingCode = it },
                        label = { Text(if (accountType == "BANK_ACCOUNT") "Routing Number (ACH / Sort Code)" else "Card CVV / Expiry (MM/YY)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("payout_routing_code")
                    )
                }

                item {
                    OutlinedTextField(
                        value = swift,
                        onValueChange = { swift = it },
                        label = { Text("SWIFT / BIC Code (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (holderName.isNotBlank() && bankName.isNotBlank() && accountNumber.isNotBlank()) {
                        onSave(accountType, holderName, bankName, accountNumber, routingCode, swift, country)
                        onDismiss()
                    }
                },
                enabled = holderName.isNotBlank() && bankName.isNotBlank() && accountNumber.isNotBlank(),
                modifier = Modifier.testTag("save_payout_account_button")
            ) {
                Text("Save Details")
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
fun WithdrawalRequestDialog(
    availableBalance: Double,
    payoutAccount: UserPayoutAccountEntity?,
    onDismiss: () -> Unit,
    onAttachDetails: () -> Unit,
    onSubmit: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Request Withdrawal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Available Balance: $${"%.2f".format(availableBalance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 48-Hour Processing Window Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                text = "48-Hour Processing Window",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Withdrawals are verified by admin and deposited to your attached bank account within 48 hours.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Payout Account Status Check
                if (payoutAccount == null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(
                                    text = "Bank / Card Details Required",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = "You must attach your bank account or debit card details before requesting a withdrawal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Button(
                                onClick = {
                                    onDismiss()
                                    onAttachDetails()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Attach Bank / Card Details")
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Destination Account:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${payoutAccount.bankOrIssuerName} (${payoutAccount.accountType.replace("_", " ")})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val masked = if (payoutAccount.accountOrCardNumber.length >= 4) "****${payoutAccount.accountOrCardNumber.takeLast(4)}" else payoutAccount.accountOrCardNumber
                                Text(
                                    text = "Account: $masked • Holder: ${payoutAccount.accountHolderName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = onAttachDetails) {
                                Text("Edit")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Withdrawal Amount ($)") },
                    singleLine = true,
                    enabled = payoutAccount != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdrawal_amount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount > 0 && amount <= availableBalance && payoutAccount != null) {
                        onSubmit(amount)
                        onDismiss()
                    }
                },
                enabled = amount > 0 && amount <= availableBalance && payoutAccount != null,
                modifier = Modifier.testTag("submit_withdrawal_button")
            ) {
                Text("Submit Withdrawal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycDocumentUploadDialog(
    initialUserName: String,
    initialEmail: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onSubmit: (
        documentType: String,
        documentNumber: String,
        country: String,
        state: String,
        expiryDate: String,
        dob: String,
        address: String,
        frontImage: String,
        backImage: String,
        selfieImage: String
    ) -> Unit
) {
    var selectedDocType by remember { mutableStateOf("DRIVING_LICENSE") }
    var docNumber by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("United States") }
    var stateOrProvince by remember { mutableStateOf("California") }
    var expiryDate by remember { mutableStateOf("2029-12-31") }
    var dateOfBirth by remember { mutableStateOf("1994-06-15") }
    var address by remember { mutableStateOf("Market Street & 4th Ave, San Francisco, CA") }

    var frontPhotoUploaded by remember { mutableStateOf(true) }
    var backPhotoUploaded by remember { mutableStateOf(true) }
    var selfieUploaded by remember { mutableStateOf(true) }

    val docTypeOptions = listOf(
        Triple("DRIVING_LICENSE", "Driving License", Icons.Default.DirectionsCar),
        Triple("NATIONAL_ID", "National ID Card", Icons.Default.Badge),
        Triple("PASSPORT", "International Passport", Icons.Default.FlightTakeoff)
    )

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
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = ServexaIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Document Verification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Required for Wallet, Payouts & POS Credit",
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
                    // Explanatory Banner
                    Surface(
                        color = ServexaTeal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = ServexaTeal,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "Government ID & POS Terminal System",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ServexaTeal
                                )
                                Text(
                                    text = "All documents are securely stored in the Admin Panel. Verified users unlock instant deposits, bank transfers, and POS Terminal credit allotment.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "1. Select Government ID Type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        docTypeOptions.forEach { (typeKey, label, icon) ->
                            val isSelected = selectedDocType == typeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDocType = typeKey },
                                leadingIcon = {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) ServexaIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "2. Document & Personal Details",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = docNumber,
                        onValueChange = { docNumber = it },
                        label = {
                            Text(
                                when (selectedDocType) {
                                    "DRIVING_LICENSE" -> "Driver's License Number (e.g. DL-CA-9938472)"
                                    "PASSPORT" -> "Passport Number (e.g. PASS-USA-89401928)"
                                    else -> "National ID / SSN Number"
                                }
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kyc_doc_number_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Issuing Country") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stateOrProvince,
                            onValueChange = { stateOrProvince = it },
                            label = { Text("State / Province") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("Expiry Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dateOfBirth,
                            onValueChange = { dateOfBirth = it },
                            label = { Text("Date of Birth") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Full Residential Address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        singleLine = false,
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kyc_address_input")
                    )
                }

                item {
                    Text(
                        text = "3. Attach ID Scans & Verification Selfie",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Take or upload clear photos of your physical document.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    // Document Front Slot
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { frontPhotoUploaded = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (frontPhotoUploaded) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = if (frontPhotoUploaded) ServexaGreen else MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "${selectedDocType.replace("_", " ")} - Front Side",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (frontPhotoUploaded) "✓ Photo captured & verified high-res" else "Tap to scan or attach front",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (frontPhotoUploaded) ServexaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            TextButton(onClick = { frontPhotoUploaded = true }) {
                                Text(if (frontPhotoUploaded) "Re-take" else "Attach")
                            }
                        }
                    }
                }

                if (selectedDocType != "PASSPORT") {
                    item {
                        // Document Back Slot
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { backPhotoUploaded = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (backPhotoUploaded) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = if (backPhotoUploaded) ServexaGreen else MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "${selectedDocType.replace("_", " ")} - Back Side (Barcode/Magnet)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (backPhotoUploaded) "✓ Back barcode scan captured" else "Tap to scan or attach back",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (backPhotoUploaded) ServexaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                TextButton(onClick = { backPhotoUploaded = true }) {
                                    Text(if (backPhotoUploaded) "Re-take" else "Attach")
                                }
                            }
                        }
                    }
                }

                item {
                    // Selfie Verification Slot
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selfieUploaded = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (selfieUploaded) Icons.Default.CheckCircle else Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (selfieUploaded) ServexaGreen else MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Live Liveness Face Selfie",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (selfieUploaded) "✓ Face liveness match completed" else "Tap to take a quick selfie",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selfieUploaded) ServexaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            TextButton(onClick = { selfieUploaded = true }) {
                                Text(if (selfieUploaded) "Re-take" else "Take Selfie")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (docNumber.isNotBlank() && address.isNotBlank()) {
                        onSubmit(
                            selectedDocType,
                            docNumber.trim(),
                            country.trim(),
                            stateOrProvince.trim(),
                            expiryDate.trim(),
                            dateOfBirth.trim(),
                            address.trim(),
                            "preview_${selectedDocType.lowercase()}_front",
                            if (selectedDocType == "PASSPORT") "" else "preview_${selectedDocType.lowercase()}_back",
                            "selfie_verified_liveness"
                        )
                        onDismiss()
                    }
                },
                enabled = docNumber.isNotBlank() && address.isNotBlank(),
                modifier = Modifier.testTag("submit_kyc_document_btn")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Submit for Verification")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
