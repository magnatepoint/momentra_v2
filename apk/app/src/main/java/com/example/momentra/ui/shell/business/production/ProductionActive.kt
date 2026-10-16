package com.example.momentra.ui.shell.business.production

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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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

private val LocalJob = staticCompositionLocalOf { false }

private val standardBg = Color(0xFF09121F)
private val standardCard = Color(0xFF122235)
private val standardNotice = Color(0xFF16405C)
private val standardBorder = Color(0xFF2B4B63)
private val standardAccent = Color(0xFF6DDCFF)
private val standardSecondary = Color(0xFFA5BBCE)
private val standardInk = Color(0xFFE5E0EE)

private val jobBg = Color(0xFF071014)
private val jobCard = Color(0xFF15262C)
private val jobBorder = Color(0xFF345058)
private val jobAccent = Color(0xFF66C7A4)
private val jobSecondary = Color(0xFFAFC1C3)
private val jobInk = Color(0xFFEFF5F2)

internal val PlantBg @Composable get() = if (LocalJob.current) jobBg else standardBg
internal val PlantCard @Composable get() = if (LocalJob.current) jobCard else standardCard
internal val PlantNotice @Composable get() = if (LocalJob.current) jobCard else standardNotice
internal val PlantField @Composable get() = if (LocalJob.current) jobBg else standardBg
internal val PlantBorder @Composable get() = if (LocalJob.current) jobBorder else standardBorder
internal val PlantBrand @Composable get() = if (LocalJob.current) jobAccent else standardAccent
internal val PlantAccent @Composable get() = if (LocalJob.current) jobAccent else standardAccent
internal val PlantSecondary @Composable get() = if (LocalJob.current) jobSecondary else standardSecondary
internal val PlantInk @Composable get() = if (LocalJob.current) jobInk else standardInk
internal val PlantSuccess @Composable get() = if (LocalJob.current) jobAccent else Color(0xFF8FD6FF)

private val dayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

@Composable
fun ProductionActive(
    model: OperatingProjectionDto,
    tab: OperatingTab,
    onOpenQuickAdd: () -> Unit,
    onRecord: suspend (ProductionEventDraft) -> Unit,
    onDecide: suspend (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activityOpen by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalJob provides (model.subtype == "MADE_TO_ORDER")) {
    Box(modifier.fillMaxSize().background(PlantBg)) {
    if (tab == OperatingTab.QUICK_ADD) {
        ProductionQuickAddHub(model, onRecord, onDecide)
    } else {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PlantBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (tab) {
            OperatingTab.PULSE -> ProductionPulse(model, onViewAll = { activityOpen = true }, onDecide = onDecide)
            OperatingTab.MOMENTS -> ProductionMoments(model, onOpenQuickAdd)
            OperatingTab.LIFE -> ProductionLife(model)
            OperatingTab.MEMORY -> ProductionMemory(model)
            OperatingTab.QUICK_ADD -> Unit
        }
    }
    }
    if (activityOpen) {
        ActivitySheet(model, onClose = { activityOpen = false })
    }
    }
    }
}

private fun pulseSubtitle(model: OperatingProjectionDto): String =
    if (model.subtype == "MADE_TO_ORDER") "Revenue qualifying delivered/billed facts, approved spend and settlement readiness."
    else "Sales, spend, collections and approved manufacturing sources."

@Composable
private fun ProductionPulse(model: OperatingProjectionDto, onViewAll: () -> Unit, onDecide: suspend (String, String) -> Unit) {
    val scope = rememberCoroutineScope()
    Header(model, "Pulse", pulseSubtitle(model))
    if (model.financeVisible) {
        SectionTitle("Target & Budget")
        PlantCard {
            val target = model.target
            val budget = model.budget
            Text("Toward the monthly target", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text(money(model, target?.progress), color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
            Text("of ${money(model, target?.value)} target", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
            Meter("Target progress", fraction(target?.progress, target?.value), PlantBrand)
            Meter("Budget used", fraction(budget?.spent, budget?.amount), PlantSuccess)
            Line("Budget remaining", money(model, budget?.remaining), PlantSuccess)
            Text("Validated sales count toward a revenue target. Collections do not. Production, orders, and deliveries count only for the measure you chose.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        SectionTitle("Needs Attention")
        if (model.attention.isEmpty()) {
            PlantCard { Text("Nothing is waiting.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
        }
        model.attention.forEach { item ->
            PlantCard {
                Text(item.kind, color = PlantAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Text(item.title, color = PlantInk, fontFamily = PlusJakartaSans, fontSize = 15.sp)
                val canDecide = item.kind == "APPROVAL" && (model.role == "OWNER_ADMIN" || model.role == "APPROVER")
                if (canDecide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Approve", color = PlantSuccess, fontFamily = PlusJakartaSans, modifier = Modifier.clickable {
                            scope.launch { onDecide(item.id, "APPROVED") }
                        })
                        Text("Reject", color = Color(0xFFF87171), fontFamily = PlusJakartaSans, modifier = Modifier.clickable {
                            scope.launch { onDecide(item.id, "REJECTED") }
                        })
                    }
                }
            }
        }
        SectionTitle("Payment Readiness")
        val ready = model.paymentReadiness.filter { it.status == "READY" }
        val expected = model.paymentReadiness.filter { it.status == "PENDING" }
        ReadinessGroup("Ready", ready, model)
        ReadinessGroup("Expected", expected, model)
        val held = model.paymentReadiness.filter { it.status != "READY" && it.status != "PENDING" }
        if (held.isNotEmpty()) ReadinessGroup("Not ready", held, model)
        if (model.paymentReadiness.isEmpty()) {
            PlantCard { Text("No open payments or collections.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
        }
        SectionTitle("Finance")
        PlantCard {
            val finance = model.finance
            Line("Qualifying revenue", money(model, model.target?.progress), PlantAccent)
            Line("Received", money(model, finance?.received), PlantSuccess)
            Line("Outstanding", money(model, finance?.outstanding), Color(0xFFFBBF24))
            Line("Eligible cost", money(model, finance?.eligibleCost), PlantInk)
            Line("Business cash paid", money(model, finance?.cashPaid), PlantInk)
        }
    } else {
        RestrictedPulse(model)
    }
    SectionTitle("Activity")
    val recent = model.activity.take(5)
    if (recent.isEmpty()) {
        PlantCard { Text("Nothing recorded yet.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
    recent.forEach { ActivityRow(it, model.financeVisible) }
    Text(
        "View all activity",
        color = PlantAccent,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, PlantBrand, RoundedCornerShape(14.dp))
            .clickable(onClick = onViewAll)
            .padding(vertical = 14.dp),
    )
}

@Composable
private fun RestrictedPulse(model: OperatingProjectionDto) {
    Text("Production status, with the work you're allowed to see.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
    PlantCard {
        Text(model.title ?: "Factory", color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text(model.role?.replace('_', ' ')?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Assigned work", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
    }
    SectionTitle("Needs Attention")
    if (model.attention.isEmpty()) {
        PlantCard { Text("Nothing is waiting.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
    model.attention.forEach { item ->
        PlantCard {
            Text(item.title, color = PlantInk, fontFamily = PlusJakartaSans, fontSize = 15.sp)
            Text(item.kind, color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
    }
    PlantCard {
        Text("Finance access restricted", color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text("Financial records, amounts, and budget history are not available in your current scope.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
    }
}

@Composable
private fun ChannelTotals(title: String, rows: List<OperatingActivityDto>, model: OperatingProjectionDto) {
    val grouped = rows.filter { it.approvalStatus != "REJECTED" && it.approvalStatus != "PENDING" }
        .groupBy { it.channelName?.ifBlank { null } ?: "Unassigned" }
    if (grouped.isEmpty()) return
    Text(title, color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    grouped.forEach { (channel, items) ->
        val total = items.sumOf { it.amount?.toDoubleOrNull() ?: 0.0 }
        Line(channel, money(model, total.toString()), PlantInk)
    }
}

@Composable
private fun ProductionMoments(model: OperatingProjectionDto, onOpenQuickAdd: () -> Unit) {
    Header(model, "Moments", "Production, dispatch, and the factory this month.")
    PlantCard {
        Text(periodLabel(model.startDate), color = PlantAccent, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        Text("This month, in motion", color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Progress", money(model, model.target?.progress), Modifier.weight(1f))
            Metric("Target", money(model, model.target?.value), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Spent", money(model, model.budget?.spent), Modifier.weight(1f))
            Metric("Remaining", money(model, model.budget?.remaining), Modifier.weight(1f))
        }
        Meter("Toward the monthly target", fraction(model.target?.progress, model.target?.value), PlantBrand)
    }
    (model.boards?.moments ?: emptyList()).forEach { section ->
        BoardBlock(section, model.financeVisible)
    }
    PlantCard {
        Text("Capture the next manufacturing event", color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(
            "Open Quick Add",
            color = PlantBg,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PlantBrand)
                .clickable(onClick = onOpenQuickAdd)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun ProductionLife(model: OperatingProjectionDto) {
    Header(model, "Life", "How production is moving today.")
    (model.boards?.life ?: emptyList()).forEach { BoardBlock(it, model.financeVisible) }
    if (model.boards?.life.isNullOrEmpty()) {
        PlantCard { Text("Nothing recorded yet.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
}

@Composable
private fun ProductionMemory(model: OperatingProjectionDto) {
    Header(model, "Memory", "What this factory keeps from the month.")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pill(periodLabel(model.startDate))
        Pill("Current")
    }
    (model.boards?.memory ?: emptyList()).forEach { BoardBlock(it, model.financeVisible) }
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
                .background(PlantBg)
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("All activity", color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                Text("Close", color = PlantSecondary, fontFamily = PlusJakartaSans, modifier = Modifier.clickable(onClick = onClose))
            }
            Text(model.title ?: "Production", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
            if (model.activity.isEmpty()) {
                Text("Nothing recorded yet.", color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            }
            model.activity.forEach { ActivityRow(it, model.financeVisible) }
        }
    }
}

@Composable
private fun Header(model: OperatingProjectionDto, title: String, body: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp)
        Pill(roleLabel(model.role))
    }
    Text(body, color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
    Text(snapshot(model.startDate), color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
}

@Composable
private fun BoardBlock(section: OperatingBoardSectionDto, financeVisible: Boolean) {
    SectionTitle(section.title)
    PlantCard {
        Text(section.detail.ifBlank { "Nothing recorded yet." }, color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        section.rows.forEach { row ->
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(row.title, color = PlantInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                val extra = listOfNotNull(row.channelName, row.counterpartyName, row.paymentStatus?.takeIf { it != "NONE" }).joinToString(" · ")
                if (extra.isNotBlank()) Text(extra, color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
                if (financeVisible && !row.amount.isNullOrBlank()) {
                    Text(row.amount, color = PlantAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(row: OperatingActivityDto, financeVisible: Boolean) {
    PlantCard {
        Text(row.eventType.replace('_', ' '), color = PlantAccent, fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Text(row.title, color = PlantInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        val bits = listOfNotNull(
            row.channelName,
            row.counterpartyName,
            if (financeVisible) row.amount else null,
        )
        if (bits.isNotEmpty()) Text(bits.joinToString(" · "), color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    }
}

@Composable
private fun ReadinessGroup(label: String, rows: List<com.example.momentra.data.api.OperatingPaymentDto>, model: OperatingProjectionDto) {
    if (rows.isEmpty()) return
    PlantCard {
        Text(label, color = PlantAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        rows.forEach { item ->
            Line(item.title, money(model, item.amount), PlantInk)
            Text(item.status, color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PlantCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PlantCard)
            .border(1.dp, PlantBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
}

@Composable
private fun Pill(text: String) {
    Text(
        text,
        color = PlantAccent,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PlantNotice)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PlantNotice)
            .padding(12.dp),
    ) {
        Text(label, color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Text(value, color = PlantInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun Meter(label: String, fraction: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = PlantSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text("${(fraction * 100).toInt()}%", color = color, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)).background(PlantBorder)) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(6.dp).background(color))
        }
    }
}

@Composable
private fun Line(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = PlantInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        Text(value, color = color, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private fun roleLabel(code: String?): String =
    OperatingMomentCatalog.roles.firstOrNull { it.first == code }?.second ?: "Production"

private fun money(model: OperatingProjectionDto, raw: String?): String {
    if (!model.financeVisible || raw.isNullOrBlank()) return "—"
    val number = raw.toBigDecimalOrNull()?.stripTrailingZeros()?.toPlainString() ?: raw
    return if (model.currency == "INR" || model.currency.isNullOrBlank()) "₹$number" else "${model.currency} $number"
}

private fun fraction(part: String?, whole: String?): Float {
    val p = part?.toFloatOrNull() ?: return 0f
    val w = whole?.toFloatOrNull() ?: return 0f
    if (w <= 0f) return 0f
    return p / w
}

private fun periodLabel(start: String?): String {
    val date = start?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() } ?: LocalDate.now()
    return date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
}

private fun snapshot(start: String?): String {
    val today = LocalDate.now().format(dayFormat)
    val from = start?.take(10)?.ifBlank { null } ?: today
    return "Snapshot · from $from · $today"
}
