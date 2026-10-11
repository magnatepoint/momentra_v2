package com.example.momentra.ui.shell.business.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentra.data.api.OperatingProjectionDto
import com.example.momentra.ui.shell.business.shared.OperatingMomentCatalog
import com.example.momentra.ui.theme.PlusJakartaSans
import kotlinx.coroutines.launch

data class RestaurantEventDraft(
    val eventType: String,
    val title: String,
    val amount: String?,
    val note: String?,
    val channelName: String?,
    val counterpartyName: String?,
    val paymentStatus: String?,
)

private enum class RestaurantSheet(
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
    REIMBURSE("Record Reimbursement", "PAYMENT", true, false, true, "PENDING"),
    PERSON("Add person", "UPDATE", false, false, true, null),
    ROLE("Role & access", "UPDATE", false, false, true, null),
    INVITE("Invite & QR", "UPDATE", false, false, true, null),
    SUPPLIER("Add supplier", "UPDATE", false, false, true, null),
}

@Composable
fun RestaurantQuickAddHub(
    model: OperatingProjectionDto,
    onRecord: suspend (RestaurantEventDraft) -> Unit,
    onDecide: suspend (String, String) -> Unit,
) {
    val allowed = model.quickAdds.map { it.code }.toSet()
    var query by remember { mutableStateOf("") }
    var sheet by remember { mutableStateOf<RestaurantSheet?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val role = model.role.orEmpty()
    val owner = role == "OWNER_ADMIN"
    val approver = role == "APPROVER" || owner
    Text("Quick Adds", color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
    Text("Record what happened · ${model.title.orEmpty()}", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HubChip(OperatingMomentCatalog.roles.firstOrNull { it.first == role }?.second ?: "Restaurant", filled = true)
        HubChip("Restaurant", filled = false)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(RestNotice)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Every restaurant moment counts.", color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text("Capture a sale, a cost, or a note while it is still clear.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
        if ("SALE" in allowed) {
            Text(
                "+ Record Sale",
                color = RestBg,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(RestBrand)
                    .clickable { sheet = RestaurantSheet.SALE }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
    BasicTextField(
        value = query,
        onValueChange = { query = it },
        textStyle = TextStyle(color = RestInk, fontFamily = PlusJakartaSans, fontSize = 14.sp),
        cursorBrush = SolidColor(RestBrand),
        decorationBox = { inner ->
            if (query.isEmpty()) Text("Search actions", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            inner()
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RestField)
            .border(1.dp, RestBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
    )
    val core = listOf(
        RestaurantSheet.SALE to "Sale",
        RestaurantSheet.PURCHASE to "Purchase",
        RestaurantSheet.SPEND to "Expense",
        RestaurantSheet.PAYMENT to "Payment",
        RestaurantSheet.COLLECTION to "Collection",
        RestaurantSheet.UPDATE to "Update",
        RestaurantSheet.ISSUE to "Issue",
        RestaurantSheet.STOCK to "Stock Update",
        RestaurantSheet.SUPPLIER to "Add Supplier",
    ).filter { (kind, label) ->
        kind.eventType in allowed && label.contains(query, ignoreCase = true)
    }
    Text("Core actions · ${core.size}", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    core.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (kind, label) ->
                ActionCard(label, locked = null, modifier = Modifier.weight(1f)) { sheet = kind }
            }
        }
    }
    Text("Support & management", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    val support = listOf(
        Triple(RestaurantSheet.REIMBURSE, "Reimbursement", if ("PAYMENT" in allowed) null else "Finance only"),
        Triple(null, "Approval review", if (approver) null else "Approver only"),
        Triple(RestaurantSheet.PERSON, "Add person", if (owner && "UPDATE" in allowed) null else "Owner only"),
        Triple(RestaurantSheet.ROLE, "Role & access", if (owner && "UPDATE" in allowed) null else "Owner only"),
        Triple(RestaurantSheet.SUPPLIER, "Suppliers & channels", if ("UPDATE" in allowed) null else "View only"),
        Triple(RestaurantSheet.INVITE, "Invite & QR", if (owner && "UPDATE" in allowed) null else "Owner only"),
    ).filter { (_, label, _) -> label.contains(query, ignoreCase = true) }
    var approvalOpen by remember { mutableStateOf(false) }
    support.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (kind, label, lock) ->
                ActionCard(label, lock, Modifier.weight(1f)) {
                    if (label == "Approval review" && approver) approvalOpen = true
                    else if (kind != null && lock == null) sheet = kind
                }
            }
        }
    }
    Text(
        "Review approvals",
        color = if (approver) RestAccent else RestSecondary,
        fontFamily = PlusJakartaSans,
        modifier = Modifier.clickable(enabled = approver) { approvalOpen = true },
    )
    if (!owner) {
        Text("Some support actions stay with the Owner or Approver.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    }
    if (error != null) Text(error ?: "", color = Color(0xFFF87171), fontFamily = PlusJakartaSans, fontSize = 13.sp)
    val open = sheet
    if (open != null) {
        RestaurantEventSheet(open, model, onDismiss = { sheet = null }) { draft ->
            onRecord(draft)
            sheet = null
        }
    }
    if (approvalOpen) {
        ApprovalSheet(model, onDismiss = { approvalOpen = false }, onDecide = onDecide)
    }
}

@Composable
private fun ActionCard(label: String, locked: String?, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(RestCard)
            .border(1.dp, RestBorder, RoundedCornerShape(14.dp))
            .clickable(enabled = locked == null, onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, color = if (locked == null) RestInk else RestSecondary, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        if (locked != null) Text(locked, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 11.sp)
    }
}

@Composable
private fun HubChip(text: String, filled: Boolean) {
    Text(
        text,
        color = if (filled) RestBg else RestSecondary,
        fontFamily = PlusJakartaSans,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (filled) RestBrand else Color.Transparent)
            .border(1.dp, if (filled) RestBrand else RestBorder, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun RestaurantEventSheet(
    kind: RestaurantSheet,
    model: OperatingProjectionDto,
    onDismiss: () -> Unit,
    onSubmit: suspend (RestaurantEventDraft) -> Unit,
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
            .background(RestNotice)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(kind.title, color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("Close", color = RestSecondary, modifier = Modifier.clickable(onClick = onDismiss))
        }
        Text("${OperatingMomentCatalog.roles.firstOrNull { it.first == model.role }?.second ?: "Restaurant"} · ${model.title.orEmpty()}", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        Field("Title", title, "What happened") { title = it }
        if (kind.needsAmount) Field("Amount", amount, "0") { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }
        if (kind.needsChannel) {
            Text("Channel", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (model.life?.channels ?: emptyList()).forEach { name ->
                    Text(
                        name,
                        color = if (name == channel) RestBg else RestSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (name == channel) RestBrand else Color.Transparent)
                            .border(1.dp, RestBorder, RoundedCornerShape(999.dp))
                            .clickable { channel = name }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
        if (kind.needsParty) {
            Field(
                if (kind == RestaurantSheet.PERSON || kind == RestaurantSheet.ROLE || kind == RestaurantSheet.INVITE) "Person" else "Name",
                party,
                "Name",
            ) { party = it }
        }
        Field("Note", note, "Optional") { note = it }
        if (localError != null) Text(localError ?: "", color = Color(0xFFF87171), fontSize = 12.sp)
        Text(
            "Save",
            color = RestBg,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RestBrand)
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
                            RestaurantEventDraft(
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
            .background(RestNotice)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Review Approval", color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("Close", color = RestSecondary, modifier = Modifier.clickable(onClick = onDismiss))
        }
        if (pending.isEmpty()) Text("Nothing is waiting.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        pending.forEach { item ->
            Text(item.title, color = RestInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Approve", color = RestSuccess, modifier = Modifier.clickable {
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
        Text(label, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        BasicTextField(
            value = value,
            onValueChange = onValue,
            textStyle = TextStyle(color = RestInk, fontFamily = PlusJakartaSans, fontSize = 14.sp),
            cursorBrush = SolidColor(RestBrand),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                inner()
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(RestField)
                .border(1.dp, RestBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
        )
    }
}
