package com.example.momentra.ui.shell.business.marketing

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

data class MarketingEventDraft(
    val eventType: String,
    val title: String,
    val amount: String?,
    val note: String?,
    val channelName: String?,
    val counterpartyName: String?,
    val paymentStatus: String?,
)

private enum class MarketingSheet(
    val title: String,
    val eventType: String,
    val needsAmount: Boolean,
    val needsChannel: Boolean,
    val needsParty: Boolean,
    val paymentStatus: String?,
) {
    SPEND("Add Spend", "SPEND", true, true, false, null),
    RESULT("Add Result", "RESULT", true, true, false, null),
    PAYMENT("Add Payment", "PAYMENT", true, false, true, "PENDING"),
    COLLECTION("Add Collection", "COLLECTION", true, true, false, "PENDING"),
    BUDGET("Add Budget", "BUDGET", true, false, false, null),
    TARGET("Add Target", "TARGET", true, false, false, null),
    UPDATE("Add Update", "UPDATE", false, false, false, null),
    ISSUE("Add Issue", "ISSUE", false, false, false, null),
    APPROVAL("Request Approval", "APPROVAL", false, false, false, null),
    DELIVERABLE("Add Deliverable", "DELIVERABLE", false, false, false, null),
    VENDOR_COST("Add Vendor Cost", "VENDOR_COST", true, false, true, null),
    COMPLETE("Mark Complete", "COMPLETE", false, false, false, null),
    PERSON("People", "UPDATE", false, false, true, null),
    ROLE("People & Roles", "UPDATE", false, false, true, null),
    PARTNER("Partners", "UPDATE", false, false, true, null),
    CHANNELS("Channels", "UPDATE", false, false, false, null),
}

@Composable
fun MarketingQuickAddHub(
    model: OperatingProjectionDto,
    onRecord: suspend (MarketingEventDraft) -> Unit,
    onDecide: suspend (String, String) -> Unit,
) {
    val allowed = model.quickAdds.map { it.code }.toSet()
    var query by remember { mutableStateOf("") }
    var sheet by remember { mutableStateOf<MarketingSheet?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val role = model.role.orEmpty()
    val owner = role == "OWNER_ADMIN"
    val approver = role == "APPROVER" || owner
    val client = model.subtype != "OWN_BUSINESS_CAMPAIGN"
    Text("Quick Adds", color = CampInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
    Text("Record what happened · ${model.title.orEmpty()}", color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HubChip(OperatingMomentCatalog.roles.firstOrNull { it.first == role }?.second ?: "Marketing", filled = true)
        HubChip("Marketing", filled = false)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (client) androidx.compose.ui.graphics.SolidColor(CampNotice)
                else androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF6C4EF2), Color(0xFF3A3842))),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            if (client) "Spend. Results. Approvals." else "Every marketing moment counts.",
            color = if (client) CampInk else Color(0xFFEFF5F2),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
        Text(
            if (client) "Record controlled spend, outcomes and handoffs." else "Capture spend, results, payments and approvals.",
            color = if (client) CampSecondary else Color(0xFFAFC1C3),
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
        )
        if (client) {
            val chips = listOfNotNull(
                model.partyName?.takeIf { it.isNotBlank() }?.let { "Client · $it" },
                model.currency?.let { "Currency · $it" },
                model.life?.channels?.takeIf { it.isNotEmpty() }?.let { "Channels · ${it.joinToString(" ")}" },
                model.budget?.amount?.let { "Budget · $it" },
                model.target?.value?.let { "Target · $it" },
            )
            chips.forEach { label ->
                Text(label, color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            }
        }
        if ("SPEND" in allowed) {
            val chipInk = if (client) CampBg else Color(0xFF66C7A4)
            val chipFill = if (client) CampBrand else Color(0xFFEFF5F2)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(chipFill)
                    .clickable { sheet = MarketingSheet.SPEND }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(painterResource(R.drawable.qa_plus), contentDescription = null, tint = chipInk, modifier = Modifier.size(14.dp))
                Text("Record Spend", color = chipInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CampField)
            .border(1.dp, CampBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(painterResource(R.drawable.qa_search), contentDescription = null, tint = CampInk, modifier = Modifier.size(18.dp))
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            textStyle = TextStyle(color = CampInk, fontFamily = PlusJakartaSans, fontSize = 14.sp),
            cursorBrush = SolidColor(CampBrand),
            decorationBox = { inner ->
                if (query.isEmpty()) Text("Search actions", color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                inner()
            },
            modifier = Modifier.weight(1f),
        )
    }
    val core = listOf(
        MarketingSheet.SPEND to "Spend",
        MarketingSheet.RESULT to "Result",
        MarketingSheet.PAYMENT to "Payment",
        MarketingSheet.COLLECTION to "Collection",
        MarketingSheet.BUDGET to "Budget",
        MarketingSheet.TARGET to "Target",
        MarketingSheet.UPDATE to "Update",
        MarketingSheet.ISSUE to "Issue",
        MarketingSheet.APPROVAL to "Approval",
        MarketingSheet.DELIVERABLE to "Deliverable",
        MarketingSheet.VENDOR_COST to "Vendor cost",
        MarketingSheet.COMPLETE to "Complete",
    ).filter { (kind, label) ->
        kind.eventType in allowed && label.contains(query, ignoreCase = true)
    }
    Text("Core actions · ${core.size}", color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    core.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (kind, label) ->
                ActionCard(label, kind.icon(client), locked = null, modifier = Modifier.weight(1f)) { sheet = kind }
            }
        }
    }
    Text("Support & management", color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    val support = if (client) {
        listOf(
            Triple(MarketingSheet.PERSON, "People", if (owner) null else "Owner only"),
            Triple(MarketingSheet.PARTNER, "Partners", if (owner) null else "Owner only"),
            Triple(MarketingSheet.CHANNELS, "Channels", if (owner) null else "Owner only"),
        )
    } else {
        listOf(
            Triple(MarketingSheet.ROLE, "People & Roles", if (owner) null else "Owner only"),
            Triple(MarketingSheet.PARTNER, "Partners & Channels", if (owner) null else "Owner only"),
        )
    }.filter { (_, label, _) -> label.contains(query, ignoreCase = true) }
    var approvalOpen by remember { mutableStateOf(false) }
    support.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (kind, label, lock) ->
                ActionCard(label, kind.icon(client), lock, Modifier.weight(1f)) {
                    if (lock == null) sheet = kind
                }
            }
        }
    }
    Text(
        "Review approvals",
        color = if (approver) CampAccent else CampSecondary,
        fontFamily = PlusJakartaSans,
        modifier = Modifier.clickable(enabled = approver) { approvalOpen = true },
    )
    if (!owner) {
        Text("Some support actions stay with the Owner or Approver.", color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    }
    if (error != null) Text(error ?: "", color = Color(0xFFF87171), fontFamily = PlusJakartaSans, fontSize = 13.sp)
    val open = sheet
    if (open != null) {
        MarketingEventSheet(open, model, onDismiss = { sheet = null }) { draft ->
            onRecord(draft)
            sheet = null
        }
    }
    if (approvalOpen) {
        ApprovalSheet(model, onDismiss = { approvalOpen = false }, onDecide = onDecide)
    }
}

private fun MarketingSheet.icon(client: Boolean): Int = when (this) {
    MarketingSheet.SPEND -> if (client) R.drawable.qa_receipt else R.drawable.qa_wallet
    MarketingSheet.RESULT -> if (client) R.drawable.qa_message else R.drawable.qa_budget
    MarketingSheet.PAYMENT -> R.drawable.qa_credit_card
    MarketingSheet.COLLECTION -> R.drawable.qa_arrow_left_right
    MarketingSheet.BUDGET, MarketingSheet.TARGET -> if (client && this == MarketingSheet.TARGET) R.drawable.qa_alert else R.drawable.qa_budget
    MarketingSheet.UPDATE -> R.drawable.qa_message
    MarketingSheet.ISSUE -> R.drawable.qa_alert
    MarketingSheet.APPROVAL -> R.drawable.qa_shield
    MarketingSheet.DELIVERABLE -> if (client) R.drawable.qa_clipboard else R.drawable.qa_message
    MarketingSheet.VENDOR_COST -> if (client) R.drawable.qa_wallet else R.drawable.qa_network
    MarketingSheet.COMPLETE -> if (client) R.drawable.qa_check else R.drawable.qa_clipboard
    MarketingSheet.PERSON -> R.drawable.qa_user_plus
    MarketingSheet.ROLE -> R.drawable.qa_users
    MarketingSheet.PARTNER -> R.drawable.qa_network
    MarketingSheet.CHANNELS -> R.drawable.qa_message
}

@Composable
private fun ActionCard(label: String, icon: Int, locked: String?, modifier: Modifier, onClick: () -> Unit) {
    val ink = if (locked == null) CampInk else CampSecondary
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CampCard)
            .border(1.dp, CampBorder, RoundedCornerShape(14.dp))
            .clickable(enabled = locked == null, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = ink, modifier = Modifier.size(28.dp))
        Text(label, color = ink, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, textAlign = TextAlign.Center)
        if (locked != null) Text(locked, color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun HubChip(text: String, filled: Boolean) {
    Text(
        text,
        color = if (filled) CampBg else CampSecondary,
        fontFamily = PlusJakartaSans,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (filled) CampBrand else Color.Transparent)
            .border(1.dp, if (filled) CampBrand else CampBorder, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun MarketingEventSheet(
    kind: MarketingSheet,
    model: OperatingProjectionDto,
    onDismiss: () -> Unit,
    onSubmit: suspend (MarketingEventDraft) -> Unit,
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
            .background(CampNotice)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(kind.title, color = CampInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("Close", color = CampSecondary, modifier = Modifier.clickable(onClick = onDismiss))
        }
        Text("${OperatingMomentCatalog.roles.firstOrNull { it.first == model.role }?.second ?: "Marketing"} · ${model.title.orEmpty()}", color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        Field("Title", title, "What happened") { title = it }
        if (kind.needsAmount) Field("Amount", amount, "0") { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }
        if (kind.needsChannel) {
            Text("Channel", color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (model.life?.channels ?: emptyList()).forEach { name ->
                    Text(
                        name,
                        color = if (name == channel) CampBg else CampSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (name == channel) CampBrand else Color.Transparent)
                            .border(1.dp, CampBorder, RoundedCornerShape(999.dp))
                            .clickable { channel = name }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
        if (kind.needsParty) {
            Field(
                if (kind == MarketingSheet.PERSON || kind == MarketingSheet.ROLE || kind == MarketingSheet.PARTNER) "Person" else "Name",
                party,
                "Name",
            ) { party = it }
        }
        Field("Note", note, "Optional") { note = it }
        if (localError != null) Text(localError ?: "", color = Color(0xFFF87171), fontSize = 12.sp)
        Text(
            "Save",
            color = CampBg,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CampBrand)
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
                            MarketingEventDraft(
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
            .background(CampNotice)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Review Approval", color = CampInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("Close", color = CampSecondary, modifier = Modifier.clickable(onClick = onDismiss))
        }
        if (pending.isEmpty()) Text("Nothing is waiting.", color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        pending.forEach { item ->
            Text(item.title, color = CampInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Approve", color = CampSuccess, modifier = Modifier.clickable {
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
        Text(label, color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        BasicTextField(
            value = value,
            onValueChange = onValue,
            textStyle = TextStyle(color = CampInk, fontFamily = PlusJakartaSans, fontSize = 14.sp),
            cursorBrush = SolidColor(CampBrand),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = CampSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                inner()
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CampField)
                .border(1.dp, CampBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
        )
    }
}
