package com.example.momentra.ui.shell.business.distribution

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

internal val DistBg = Color(0xFF071014)
internal val DistCard = Color(0xFF0C171B)
internal val DistNotice = Color(0xFF15262C)
internal val DistField = Color(0xFF0C171B)
internal val DistBorder = Color(0xFF345058)
internal val DistBrand = Color(0xFF66C7A4)
internal val DistAccent = Color(0xFF66C7A4)
internal val DistSecondary = Color(0xFF66C7A4)
internal val DistInk = Color(0xFFEFF5F2)
internal val DistSuccess = Color(0xFF66C7A4)

private val dayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

@Composable
fun DistributionActive(
    model: OperatingProjectionDto,
    tab: OperatingTab,
    onOpenQuickAdd: () -> Unit,
    onRecord: suspend (DistributionEventDraft) -> Unit,
    onDecide: suspend (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activityOpen by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize().background(DistBg)) {
    if (tab == OperatingTab.QUICK_ADD) {
        DistributionQuickAddHub(model, onRecord, onDecide)
    } else {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DistBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (tab) {
            OperatingTab.PULSE -> DistributionPulse(model, onViewAll = { activityOpen = true })
            OperatingTab.MOMENTS -> DistributionMoments(model, onOpenQuickAdd)
            OperatingTab.LIFE -> DistributionLife(model)
            OperatingTab.MEMORY -> DistributionMemory(model)
            OperatingTab.QUICK_ADD -> Unit
        }
    }
    }
    if (activityOpen) {
        ActivitySheet(model, onClose = { activityOpen = false })
    }
    }
}

@Composable
private fun DistributionPulse(model: OperatingProjectionDto, onViewAll: () -> Unit) {
    Header(model, "Pulse", "Your sales plan, distribution commitments and ready money.")
    if (model.financeVisible) {
        SectionTitle("Target & Budget")
        DistCard {
            val target = model.target
            val budget = model.budget
            Text("Recorded sales", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text(money(model, target?.progress), color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
            Text("of ${money(model, target?.value)} sales target", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
            Meter("Sales progress", fraction(target?.progress, target?.value), DistBrand)
            Meter("Budget used", fraction(budget?.spent, budget?.amount), DistSuccess)
            Line("Budget remaining", money(model, budget?.remaining), DistSuccess)
            Text("Validated sales count toward the target. Eligible purchase and expense facts count once toward budget.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        SectionTitle("Needs Attention")
        if (model.attention.isEmpty()) {
            DistCard { Text("Nothing is waiting.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
        }
        model.attention.forEach { item ->
            DistCard {
                Text(item.kind, color = DistAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Text(item.title, color = DistInk, fontFamily = PlusJakartaSans, fontSize = 15.sp)
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
            DistCard { Text("No open payments or collections.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
        }
        SectionTitle("Finance")
        DistCard {
            val finance = model.finance
            Line("Recorded sales", money(model, finance?.sales), DistAccent)
            Line("Received", money(model, finance?.received), DistSuccess)
            Line("Outstanding", money(model, finance?.outstanding), Color(0xFFFBBF24))
            Line("Eligible cost", money(model, finance?.eligibleCost), DistInk)
            Line("Business cash paid", money(model, finance?.cashPaid), DistInk)
        }
    } else {
        StaffPulse(model)
    }
    SectionTitle("Activity")
    val recent = model.activity.take(5)
    if (recent.isEmpty()) {
        DistCard { Text("Nothing recorded yet.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
    recent.forEach { ActivityRow(it, model.financeVisible) }
    Text(
        "View all activity",
        color = DistAccent,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DistBrand, RoundedCornerShape(14.dp))
            .clickable(onClick = onViewAll)
            .padding(vertical = 14.dp),
    )
}

@Composable
private fun StaffPulse(model: OperatingProjectionDto) {
    Pill(roleLabel(model.role))
    DistCard {
        Text("Your view", color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text("Assigned work stays visible. Protected amounts stay hidden.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
    }
    model.attention.forEach { item ->
        DistCard {
            Text(item.title, color = DistInk, fontFamily = PlusJakartaSans, fontSize = 15.sp)
            Text(item.kind, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
    }
    DistCard {
        Text("Finance totals stay with Finance, Approver, and Owner.", color = DistInk, fontFamily = PlusJakartaSans, fontSize = 15.sp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun DistributionMoments(model: OperatingProjectionDto, onOpenQuickAdd: () -> Unit) {
    Header(model, "Moments", "Dispatches, stock, and the distribution this month.")
    DistCard {
        Text(periodLabel(model.startDate), color = DistAccent, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        Text("This month, in motion", color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Sales", money(model, model.finance?.sales ?: model.target?.progress), Modifier.weight(1f))
            Metric("Target", money(model, model.target?.value), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Spent", money(model, model.budget?.spent), Modifier.weight(1f))
            Metric("Remaining", money(model, model.budget?.remaining), Modifier.weight(1f))
        }
        Meter("Sales toward target", fraction(model.target?.progress, model.target?.value), DistBrand)
    }
    (model.boards?.moments ?: emptyList()).forEach { section ->
        BoardBlock(section, model.financeVisible)
    }
    DistCard {
        Text("Create the next distribution moment", color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(
            "Open Quick Add",
            color = DistBg,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DistBrand)
                .clickable(onClick = onOpenQuickAdd)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun DistributionLife(model: OperatingProjectionDto) {
    Header(model, "Life", "How the distribution is moving today.")
    (model.boards?.life ?: emptyList()).forEach { BoardBlock(it, model.financeVisible) }
    if (model.boards?.life.isNullOrEmpty()) {
        DistCard { Text("Nothing recorded yet.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
}

@Composable
private fun DistributionMemory(model: OperatingProjectionDto) {
    Header(model, "Memory", "What this distribution keeps from the month.")
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
                .background(DistBg)
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("All activity", color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                Text("Close", color = DistSecondary, fontFamily = PlusJakartaSans, modifier = Modifier.clickable(onClick = onClose))
            }
            Text(model.title ?: "Distribution", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
            if (model.activity.isEmpty()) {
                Text("Nothing recorded yet.", color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            }
            model.activity.forEach { ActivityRow(it, model.financeVisible) }
        }
    }
}

@Composable
private fun Header(model: OperatingProjectionDto, title: String, body: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp)
        Pill(roleLabel(model.role))
    }
    Text(body, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
    Text(snapshot(model.startDate), color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
}

@Composable
private fun BoardBlock(section: OperatingBoardSectionDto, financeVisible: Boolean) {
    SectionTitle(section.title)
    DistCard {
        Text(section.detail.ifBlank { "Nothing recorded yet." }, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        section.rows.forEach { row ->
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(row.title, color = DistInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                val extra = listOfNotNull(row.channelName, row.counterpartyName, row.paymentStatus?.takeIf { it != "NONE" }).joinToString(" · ")
                if (extra.isNotBlank()) Text(extra, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
                if (financeVisible && !row.amount.isNullOrBlank()) {
                    Text(row.amount, color = DistAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(row: OperatingActivityDto, financeVisible: Boolean) {
    DistCard {
        Text(row.eventType.replace('_', ' '), color = DistAccent, fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Text(row.title, color = DistInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        val bits = listOfNotNull(
            row.channelName,
            row.counterpartyName,
            if (financeVisible) row.amount else null,
        )
        if (bits.isNotEmpty()) Text(bits.joinToString(" · "), color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    }
}

@Composable
private fun ReadinessGroup(label: String, rows: List<com.example.momentra.data.api.OperatingPaymentDto>, model: OperatingProjectionDto) {
    if (rows.isEmpty()) return
    DistCard {
        Text(label, color = DistAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        rows.forEach { item ->
            Line(item.title, money(model, item.amount), DistInk)
            Text(item.status, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DistCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DistCard)
            .border(1.dp, DistBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
}

@Composable
private fun Pill(text: String) {
    Text(
        text,
        color = DistAccent,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DistNotice)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DistNotice)
            .padding(12.dp),
    ) {
        Text(label, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Text(value, color = DistInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun Meter(label: String, fraction: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = DistSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text("${(fraction * 100).toInt()}%", color = color, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)).background(DistBorder)) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(6.dp).background(color))
        }
    }
}

@Composable
private fun Line(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = DistInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        Text(value, color = color, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private fun roleLabel(code: String?): String =
    OperatingMomentCatalog.roles.firstOrNull { it.first == code }?.second ?: "Distribution"

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
