package com.example.momentra.ui.shell.business.distribution

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentra.R
import com.example.momentra.data.api.OperatingProjectionDto
import com.example.momentra.ui.shell.business.shared.OperatingMomentCatalog
import com.example.momentra.ui.theme.PlusJakartaSans
import kotlinx.coroutines.launch

data class DistributionEventDraft(
    val eventType: String,
    val title: String,
    val amount: String?,
    val note: String?,
    val channelName: String?,
    val counterpartyName: String?,
    val paymentStatus: String?,
)

private enum class DistributionSheet(
    val title: String,
    val eventType: String,
    val needsAmount: Boolean,
    val needsChannel: Boolean,
    val needsParty: Boolean,
    val paymentStatus: String?,
) {
    SALE("Record Sale", "SALE", true, true, false, null),
    PURCHASE("Record Purchase", "PURCHASE", true, false, true, null),
    SPEND("Record Expense", "SPEND", true, false, false, null),
    PAYMENT("Record Payment", "PAYMENT", true, false, true, "PENDING"),
    COLLECTION("Record Collection", "COLLECTION", true, true, false, "PENDING"),
    BUDGET("Revise Budget", "BUDGET", true, false, false, null),
    UPDATE("Post Update", "UPDATE", false, false, false, null),
    ISSUE("Report Issue", "ISSUE", false, false, false, null),
    STOCK("Stock Update", "STOCK", false, false, false, null),
    ORDER("Order Update", "ORDER", false, false, false, null),
    SHIPMENT("Shipment Update", "SHIPMENT", false, false, false, null),
    REIMBURSE("Record Reimbursement", "PAYMENT", true, false, true, "PENDING"),
    PERSON("Add person", "UPDATE", false, false, true, null),
    ROLE("Role & access", "UPDATE", false, false, true, null),
    INVITE("Invite & QR", "UPDATE", false, false, true, null),
    SUPPLIER("Add supplier", "UPDATE", false, false, true, null),
}

@Composable
fun DistributionQuickAddHub(
    model: OperatingProjectionDto,
    onRecord: suspend (DistributionEventDraft) -> Unit,
    onDecide: suspend (String, String) -> Unit,
) {
    val allowed = model.quickAdds.map { it.code }.toSet()
    var query by remember { mutableStateOf("") }
    var sheet by remember { mutableStateOf<DistributionSheet?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val role = model.role.orEmpty()
    val owner = role == "OWNER_ADMIN"
    val approver = role == "APPROVER" || owner
    Text("Quick Adds", color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
    Text("Record what happened · ${model.title.orEmpty()}", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HubChip(OperatingMomentCatalog.roles.firstOrNull { it.first == role }?.second ?: "Distribution", filled = true)
        HubChip("Distribution", filled = false)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DistNotice)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Move. Sell. Collect.", color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text("Capture a sale, a cost, or a note while it is still clear.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
        if ("SALE" in allowed) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(DistBrand)
                    .clickable { sheet = DistributionSheet.SALE }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(painterResource(R.drawable.qa_plus), contentDescription = null, tint = DistBg, modifier = Modifier.size(14.dp))
                Text("Record Sale", color = DistBg, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DistField)
            .border(1.dp, DistBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(painterResource(R.drawable.qa_search), contentDescription = null, tint = DistSecondary, modifier = Modifier.size(18.dp))
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            textStyle = TextStyle(color = DistInk, fontFamily = PlusJakartaSans, fontSize = 14.sp),
            cursorBrush = SolidColor(DistBrand),
            decorationBox = { inner ->
                if (query.isEmpty()) Text("Search actions", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                inner()
            },
            modifier = Modifier.weight(1f),
        )
    }
    val core = listOf(
        DistributionSheet.SALE to "Sale",
        DistributionSheet.PURCHASE to "Purchase",
        DistributionSheet.SPEND to "Expense",
        DistributionSheet.PAYMENT to "Payment",
        DistributionSheet.COLLECTION to "Collection",
        DistributionSheet.UPDATE to "Update",
        DistributionSheet.ISSUE to "Issue",
        DistributionSheet.STOCK to "Stock",
        DistributionSheet.ORDER to "Order",
        DistributionSheet.SHIPMENT to "Shipment",
    ).filter { (kind, label) ->
        kind.eventType in allowed && label.contains(query, ignoreCase = true)
    }
    Text("Core actions · ${core.size}", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    core.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (kind, label) ->
                ActionCard(label, kind.hubIcon, locked = null, modifier = Modifier.weight(1f)) { sheet = kind }
            }
        }
    }
    Text("Support & management", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    val support = listOf(
        Triple(DistributionSheet.REIMBURSE, "Reimbursement", if ("PAYMENT" in allowed) null else "Finance only"),
        Triple(null, "Approval review", if (approver) null else "Approver only"),
        Triple(DistributionSheet.PERSON, "Add person", if (owner && "UPDATE" in allowed) null else "Owner only"),
        Triple(DistributionSheet.ROLE, "Role & access", if (owner && "UPDATE" in allowed) null else "Owner only"),
        Triple(DistributionSheet.SUPPLIER, "Suppliers & channels", if ("UPDATE" in allowed) null else "View only"),
        Triple(DistributionSheet.INVITE, "Invite & QR", if (owner && "UPDATE" in allowed) null else "Owner only"),
    ).filter { (_, label, _) -> label.contains(query, ignoreCase = true) }
    var approvalOpen by remember { mutableStateOf(false) }
    support.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (kind, label, lock) ->
                ActionCard(label, if (kind == null) R.drawable.qa_shield else kind.hubIcon, lock, Modifier.weight(1f)) {
                    if (label == "Approval review" && approver) approvalOpen = true
                    else if (kind != null && lock == null) sheet = kind
                }
            }
        }
    }
    Text(
        "Review approvals",
        color = if (approver) DistAccent else DistSecondary,
        fontFamily = PlusJakartaSans,
        modifier = Modifier.clickable(enabled = approver) { approvalOpen = true },
    )
    if (!owner) {
        Text("Some support actions stay with the Owner or Approver.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    }
    if (error != null) Text(error ?: "", color = Color(0xFFF87171), fontFamily = PlusJakartaSans, fontSize = 13.sp)
    val open = sheet
    if (open != null) {
        DistributionEventSheet(open, model, onDismiss = { sheet = null }) { draft ->
            onRecord(draft)
            sheet = null
        }
    }
    if (approvalOpen) {
        ApprovalSheet(model, onDismiss = { approvalOpen = false }, onDecide = onDecide)
    }
}

private val DistributionSheet.hubIcon: Int
    get() = when (this) {
        DistributionSheet.SALE, DistributionSheet.SPEND -> R.drawable.qa_receipt
        DistributionSheet.PURCHASE -> R.drawable.qa_wallet
        DistributionSheet.PAYMENT, DistributionSheet.REIMBURSE -> R.drawable.qa_credit_card
        DistributionSheet.COLLECTION -> R.drawable.qa_arrow_left_right
        DistributionSheet.BUDGET -> R.drawable.qa_budget
        DistributionSheet.UPDATE -> R.drawable.qa_message
        DistributionSheet.ISSUE -> R.drawable.qa_alert
        DistributionSheet.STOCK -> R.drawable.qa_package
        DistributionSheet.ORDER -> R.drawable.qa_truck
        DistributionSheet.SHIPMENT -> R.drawable.qa_package
        DistributionSheet.PERSON -> R.drawable.qa_user_plus
        DistributionSheet.ROLE -> R.drawable.qa_users
        DistributionSheet.INVITE -> R.drawable.qa_qr
        DistributionSheet.SUPPLIER -> R.drawable.qa_network
    }

@Composable
private fun ActionCard(label: String, icon: Int, locked: String?, modifier: Modifier, onClick: () -> Unit) {
    val ink = if (locked == null) DistInk else DistSecondary
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DistCard)
            .border(1.dp, DistBorder, RoundedCornerShape(14.dp))
            .clickable(enabled = locked == null, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = ink, modifier = Modifier.size(28.dp))
        Text(label, color = ink, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, textAlign = TextAlign.Center)
        if (locked != null) Text(locked, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun HubChip(text: String, filled: Boolean) {
    Text(
        text,
        color = if (filled) DistBg else DistSecondary,
        fontFamily = PlusJakartaSans,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (filled) DistBrand else Color.Transparent)
            .border(1.dp, if (filled) DistBrand else DistBorder, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun DistributionEventSheet(
    kind: DistributionSheet,
    model: OperatingProjectionDto,
    onDismiss: () -> Unit,
    onSubmit: suspend (DistributionEventDraft) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf(model.life?.channels?.firstOrNull().orEmpty()) }
    var party by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DistNotice)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(kind.title, color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("Close", color = DistSecondary, modifier = Modifier.clickable(onClick = onDismiss))
        }
        Text("${OperatingMomentCatalog.roles.firstOrNull { it.first == model.role }?.second ?: "Distribution"} · ${model.title.orEmpty()}", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        Field("Title", title, "What happened") { title = it }
        if (kind.needsAmount) Field("Amount", amount, "0") { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }
        if (kind.needsChannel) {
            Text("Channel", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (model.life?.channels ?: emptyList()).forEach { name ->
                    Text(
                        name,
                        color = if (name == channel) DistBg else DistSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (name == channel) DistBrand else Color.Transparent)
                            .border(1.dp, DistBorder, RoundedCornerShape(999.dp))
                            .clickable { channel = name }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
        if (kind.needsParty) {
            Field(
                if (kind == DistributionSheet.PERSON || kind == DistributionSheet.ROLE || kind == DistributionSheet.INVITE) "Person" else "Name",
                party,
                "Name",
            ) { party = it }
        }
        Field("Note", note, "Optional") { note = it }
        if (localError != null) Text(localError ?: "", color = Color(0xFFF87171), fontSize = 12.sp)
        Text(
            "Save",
            color = DistBg,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DistBrand)
                .clickable {
                    val trimmed = title.trim().ifBlank { party.trim() }
                    if (trimmed.isEmpty()) {
                        localError = "A title is required."
                        return@clickable
                    }
                    if (kind.needsAmount && amount.toDoubleOrNull()?.let { it > 0 } != true) {
                        localError = "Amount must be positive."
                        return@clickable
                    }
                    localError = null
                    scope.launch {
                        onSubmit(
                            DistributionEventDraft(
                                eventType = kind.eventType,
                                title = trimmed,
                                amount = amount.ifBlank { null },
                                note = note.ifBlank { null },
                                channelName = channel.ifBlank { null },
                                counterpartyName = party.ifBlank { null },
                                paymentStatus = kind.paymentStatus,
                            )
                        )
                    }
                }
                .padding(vertical = 14.dp),
        )
    }
}

@Composable
private fun ApprovalSheet(
    model: OperatingProjectionDto,
    onDismiss: () -> Unit,
    onDecide: suspend (String, String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pending = model.attention.filter { it.kind == "APPROVAL" }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DistNotice)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Review Approval", color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("Close", color = DistSecondary, modifier = Modifier.clickable(onClick = onDismiss))
        }
        if (pending.isEmpty()) Text("Nothing is waiting.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        pending.forEach { item ->
            Text(item.title, color = DistInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Approve", color = DistSuccess, modifier = Modifier.clickable {
                    scope.launch { onDecide(item.id, "APPROVED"); onDismiss() }
                })
                Text("Reject", color = Color(0xFFF87171), modifier = Modifier.clickable {
                    scope.launch { onDecide(item.id, "REJECTED"); onDismiss() }
                })
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, placeholder: String, onValue: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        BasicTextField(
            value = value,
            onValueChange = onValue,
            textStyle = TextStyle(color = DistInk, fontFamily = PlusJakartaSans, fontSize = 14.sp),
            cursorBrush = SolidColor(DistBrand),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                inner()
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DistField)
                .border(1.dp, DistBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
        )
    }
}
