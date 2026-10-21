package com.example.momentra.ui.shell.business.retail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentra.data.api.OperatingActivityDto
import com.example.momentra.data.api.OperatingBoardSectionDto
import com.example.momentra.data.api.OperatingProjectionDto
import com.example.momentra.ui.shell.business.shared.OperatingMomentCatalog
import com.example.momentra.ui.shell.business.shared.OperatingTab
import com.example.momentra.ui.theme.PlusJakartaSans
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val StoreBg = Color(0xFF0B1020)
internal val StoreCard = Color(0xFF161B2E)
internal val StoreNotice = Color(0xFF252654)
internal val StoreField = Color(0xFF1E243D)
internal val StoreBorder = Color(0xFF2D3554)
internal val StoreBrand = Color(0xFF5B5CEB)
internal val StoreAccent = Color(0xFFA5A7FF)
internal val StoreSecondary = Color(0xFFA1A7C1)
internal val StoreSuccess = Color(0xFF10B981)
internal val StoreSuccessBg = Color(0xFF102C30)

private val dayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

@Composable
fun RetailStoreActive(
    model: OperatingProjectionDto,
    tab: OperatingTab,
    onOpenQuickAdd: () -> Unit,
    onRecord: suspend (RetailEventDraft) -> Unit,
    onDecide: suspend (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activityOpen by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize().background(StoreBg)) {
    if (tab == OperatingTab.QUICK_ADD) {
        RetailQuickAddHub(model, onRecord, onDecide)
    } else {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StoreBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (tab) {
            OperatingTab.PULSE -> RetailPulse(model, onViewAll = { activityOpen = true })
            OperatingTab.MOMENTS -> RetailMoments(model, onOpenQuickAdd)
            OperatingTab.LIFE -> RetailLife(model)
            OperatingTab.MEMORY -> RetailMemory(model)
            OperatingTab.QUICK_ADD -> Unit
        }
    }
    }
    if (activityOpen) {
        ActivitySheet(model, onClose = { activityOpen = false })
    }
    com.example.momentra.ui.shell.business.shared.OperatingChromeSheets(model)
    }
}

@Composable
private fun RetailPulse(model: OperatingProjectionDto, onViewAll: () -> Unit) {
    Header(model, "Pulse", "Your sales plan, store commitments and ready money.")
    if (model.financeVisible) {
        SectionTitle("Target & Budget")
        StoreCard {
            val target = model.target
            val budget = model.budget
            Text("Recorded sales", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text(money(model, target?.progress), color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
            Text("of ${money(model, target?.value)} sales target", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
            Meter("Sales progress", fraction(target?.progress, target?.value), StoreBrand)
            Meter("Budget used", fraction(budget?.spent, budget?.amount), StoreSuccess)
            Line("Budget remaining", money(model, budget?.remaining), StoreSuccess)
            Text("Validated sales count toward the target. Eligible purchase and expense facts count once toward budget.", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        SectionTitle("Needs Attention")
        if (model.attention.isEmpty()) {
            StoreCard { Text("Nothing is waiting.", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
        }
        model.attention.forEach { item ->
            StoreCard {
                Text(item.kind, color = StoreAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Text(item.title, color = Color.White, fontFamily = PlusJakartaSans, fontSize = 15.sp)

                if (item.id.startsWith("open-")) {
                    val payment = model.paymentReadiness.firstOrNull { it.id == item.id.removePrefix("open-") }
                    if (payment != null) {
                        Text(
                            "Open",
                            color = Color.White,
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { com.example.momentra.ui.shell.business.shared.OperatingChromeState.settling = payment },
                        )
                    }
                }
            }
        }
        SectionTitle("Payment Readiness")
        val toCollect = model.paymentReadiness.filter { it.relevance == "RECEIVABLE" }
        val toPay = model.paymentReadiness.filter { it.relevance != "RECEIVABLE" }
        ReadinessGroup("To collect", toCollect, model)
        ReadinessGroup("To pay", toPay, model)
        if (model.paymentReadiness.isEmpty()) {
            StoreCard { Text("No open payments or collections.", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
        }
        SectionTitle("Finance")
        StoreCard {
            val finance = model.finance
            Line("Recorded sales", money(model, finance?.sales), StoreAccent)
            Line("Received", money(model, finance?.received), StoreSuccess)
            Line("Still to collect", money(model, finance?.outstanding), Color(0xFFFBBF24))
            Line("Eligible cost", money(model, finance?.eligibleCost), Color.White)
            Line("Business cash paid", money(model, finance?.cashPaid), Color.White)
            Line("Still to pay", money(model, finance?.payable), Color(0xFFFBBF24))
        }
    } else {
        StaffPulse(model)
    }
    SectionTitle("Activity")
    val recent = model.activity.take(5)
    if (recent.isEmpty()) {
        StoreCard { Text("Nothing recorded yet.", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
    recent.forEach { ActivityRow(it, model) }
    Text(
        "View all activity",
        color = StoreAccent,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, StoreBrand, RoundedCornerShape(14.dp))
            .clickable(onClick = onViewAll)
            .padding(vertical = 14.dp),
    )
}

@Composable
private fun StaffPulse(model: OperatingProjectionDto) {
    Pill(roleLabel(model.role))
    StoreCard {
        Text("Your view", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text("Assigned work stays visible. Protected amounts stay hidden.", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
    }
    model.attention.forEach { item ->
        StoreCard {
            Text(item.title, color = Color.White, fontFamily = PlusJakartaSans, fontSize = 15.sp)
            Text(item.kind, color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
    }
    StoreCard {
        Text("Finance totals stay with Finance, Approver, and Owner.", color = Color.White, fontFamily = PlusJakartaSans, fontSize = 15.sp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun RetailMoments(model: OperatingProjectionDto, onOpenQuickAdd: () -> Unit) {
    Header(model, "Moments", "Sales, stock, and the store this month.")
    StoreCard {
        Text(periodLabel(model.startDate), color = StoreAccent, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        Text("This month, in motion", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Sales", money(model, model.finance?.sales ?: model.target?.progress), Modifier.weight(1f))
            Metric("Target", money(model, model.target?.value), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Spent", money(model, model.budget?.spent), Modifier.weight(1f))
            Metric("Remaining", money(model, model.budget?.remaining), Modifier.weight(1f))
        }
        Meter("Sales toward target", fraction(model.target?.progress, model.target?.value), StoreBrand)
    }
    listOf("sales", "purchases", "updates", "stock", "issues", "expenses", "payments", "channels").forEach { code ->
        val section = model.boards?.moments?.firstOrNull { it.code == code } ?: return@forEach
        BoardBlock(section, model)
    }
    StoreCard {
        Text("Create the next store moment", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(
            "Open Quick Add",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(StoreBrand)
                .clickable(onClick = onOpenQuickAdd)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun RetailLife(model: OperatingProjectionDto) {
    Header(model, "Life", "How the store is moving today.")
    (model.boards?.life ?: emptyList()).forEach { BoardBlock(it, model) }
    if (model.boards?.life.isNullOrEmpty()) {
        StoreCard { Text("Nothing recorded yet.", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
}

@Composable
private fun RetailMemory(model: OperatingProjectionDto) {
    Header(model, "Memory", "What this store keeps from the month.")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pill(periodLabel(model.startDate))
        Pill("Current")
    }
    (model.boards?.memory ?: emptyList()).forEach { BoardBlock(it, model) }
}

@Composable
private fun ActivitySheet(model: OperatingProjectionDto, onClose: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClose)
            .padding(top = 80.dp),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(StoreBg)
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("All activity", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                Text("Close", color = StoreSecondary, fontFamily = PlusJakartaSans, modifier = Modifier.clickable(onClick = onClose))
            }
            Text(model.title ?: "Retail Store", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
            if (model.activity.isEmpty()) {
                Text("Nothing recorded yet.", color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            }
            model.activity.forEach { ActivityRow(it, model) }
        }
    }
}

@Composable
private fun Header(model: OperatingProjectionDto, title: String, body: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp)
        Pill(roleLabel(model.role))
    }
    Text(body, color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
    Text(snapshot(model.startDate, model.endDate), color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
}

@Composable
private fun BoardBlock(section: OperatingBoardSectionDto, model: OperatingProjectionDto) {
    SectionTitle(section.title)
    StoreCard {
        Text(com.example.momentra.ui.shell.business.shared.formatOperatingDetail(section.detail.ifBlank { "Nothing recorded yet." }, model.currency), color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        section.rows.forEach { row ->
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(row.title, color = Color.White, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                val extra = listOfNotNull(row.channelName, row.counterpartyName, row.paymentStatus?.takeIf { it != "NONE" }).joinToString(" · ")
                if (extra.isNotBlank()) Text(extra, color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
                if (model.financeVisible && !row.amount.isNullOrBlank()) {
                    Text(com.example.momentra.ui.shell.business.shared.formatOperatingAmount(model.currency, row.amount, true), color = StoreAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(row: OperatingActivityDto, model: OperatingProjectionDto) {
    StoreCard {
        Text(row.eventType.replace('_', ' '), color = StoreAccent, fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Text(row.title, color = Color.White, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        val qty = listOfNotNull(row.quantity, row.unit).filter { it.isNotBlank() }.joinToString(" ")
        val bits = listOfNotNull(
            row.counterpartyName,
            qty.ifBlank { null },
            row.paymentStatus?.takeIf { it != "NONE" },
            row.occurredAt.take(10).ifBlank { null },
            if (model.financeVisible) com.example.momentra.ui.shell.business.shared.formatOperatingAmount(model.currency, row.amount, true).takeIf { it != "—" } else null,
        )
        Text(
            "Edit",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { com.example.momentra.ui.shell.business.shared.OperatingChromeState.editing = row },
        )
        if (bits.isNotEmpty()) Text(bits.joinToString(" · "), color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    }
}

@Composable
private fun ReadinessGroup(label: String, rows: List<com.example.momentra.data.api.OperatingPaymentDto>, model: OperatingProjectionDto) {
    if (rows.isEmpty()) return
    StoreCard {
        Text(label, color = StoreAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        rows.forEach { item ->
            Column(Modifier.clickable { com.example.momentra.ui.shell.business.shared.OperatingChromeState.settling = item }) {
                Line(item.title, money(model, item.amount), Color.White)
                val meta = listOfNotNull(item.counterpartyName, item.dueAt?.take(10)).filter { it.isNotBlank() }.joinToString(" · ")
                if (meta.isNotBlank()) Text(meta, color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StoreCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(StoreCard)
            .border(1.dp, StoreBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
}

@Composable
private fun Pill(text: String) {
    Text(
        text,
        color = StoreAccent,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(StoreNotice)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(StoreNotice)
            .padding(12.dp),
    ) {
        Text(label, color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Text(value, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun Meter(label: String, fraction: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = StoreSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text("${(fraction * 100).toInt()}%", color = color, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)).background(StoreBorder)) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(6.dp).background(color))
        }
    }
}

@Composable
private fun Line(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        Text(value, color = color, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private fun roleLabel(code: String?): String =
    OperatingMomentCatalog.roles.firstOrNull { it.first == code }?.second ?: "Retail Store"

private fun money(model: OperatingProjectionDto, raw: String?): String =
    com.example.momentra.ui.shell.business.shared.formatOperatingAmount(model.currency, raw, model.financeVisible)

private fun fraction(part: String?, whole: String?): Float {
    val p = part?.toFloatOrNull() ?: return 0f
    val w = whole?.toFloatOrNull() ?: return 0f
    if (w <= 0f) return 0f
    return p / w
}

private fun periodLabel(start: String?): String {
    val date = start?.let { runCatching { java.time.LocalDate.parse(it.take(10)) }.getOrNull() } ?: java.time.LocalDate.now()
    return date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.ENGLISH))
}

private fun snapshot(start: String?, end: String? = null): String {
    val today = java.time.LocalDate.now().format(dayFormat)
    val from = start?.take(10)?.ifBlank { null } ?: today
    val until = end?.take(10)?.ifBlank { null }
    return if (until != null) "Snapshot · $from – $until" else "Snapshot · from $from · $today"
}
