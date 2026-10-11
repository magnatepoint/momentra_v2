package com.example.momentra.ui.shell.business.restaurant

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

internal val RestBg = Color(0xFF101412)
internal val RestCard = Color(0xFF202A24)
internal val RestNotice = Color(0xFF2A352E)
internal val RestField = Color(0xFF1A211D)
internal val RestBorder = Color(0xFF344238)
internal val RestBrand = Color(0xFFF2B544)
internal val RestAccent = Color(0xFFF2B544)
internal val RestSecondary = Color(0xFFB9C5BA)
internal val RestInk = Color(0xFFF4F3EB)
internal val RestSuccess = Color(0xFF8FBF7A)

private val dayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

@Composable
fun RestaurantActive(
    model: OperatingProjectionDto,
    tab: OperatingTab,
    onOpenQuickAdd: () -> Unit,
    onRecord: suspend (RestaurantEventDraft) -> Unit,
    onDecide: suspend (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activityOpen by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RestBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (tab) {
            OperatingTab.PULSE -> RestaurantPulse(model, onViewAll = { activityOpen = true })
            OperatingTab.MOMENTS -> RestaurantMoments(model, onOpenQuickAdd)
            OperatingTab.LIFE -> RestaurantLife(model)
            OperatingTab.MEMORY -> RestaurantMemory(model)
            OperatingTab.QUICK_ADD -> RestaurantQuickAddHub(model, onRecord, onDecide)
        }
    }
    if (activityOpen) {
        ActivitySheet(model, onClose = { activityOpen = false })
    }
    }
}

@Composable
private fun RestaurantPulse(model: OperatingProjectionDto, onViewAll: () -> Unit) {
    Header(model, "Pulse", "Your sales plan, restaurant commitments and ready money.")
    if (model.financeVisible) {
        SectionTitle("Target & Budget")
        RestCard {
            val target = model.target
            val budget = model.budget
            Text("Recorded sales", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text(money(model, target?.progress), color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
            Text("of ${money(model, target?.value)} sales target", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
            Meter("Sales progress", fraction(target?.progress, target?.value), RestBrand)
            Meter("Budget used", fraction(budget?.spent, budget?.amount), RestSuccess)
            Line("Budget remaining", money(model, budget?.remaining), RestSuccess)
            Text("Validated sales count toward the target. Eligible purchase and expense facts count once toward budget.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        SectionTitle("Needs Attention")
        if (model.attention.isEmpty()) {
            RestCard { Text("Nothing is waiting.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
        }
        model.attention.forEach { item ->
            RestCard {
                Text(item.kind, color = RestAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Text(item.title, color = RestInk, fontFamily = PlusJakartaSans, fontSize = 15.sp)
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
            RestCard { Text("No open payments or collections.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
        }
        SectionTitle("Finance")
        RestCard {
            val finance = model.finance
            Line("Recorded sales", money(model, finance?.sales), RestAccent)
            Line("Received", money(model, finance?.received), RestSuccess)
            Line("Outstanding", money(model, finance?.outstanding), Color(0xFFFBBF24))
            Line("Eligible cost", money(model, finance?.eligibleCost), RestInk)
            Line("Business cash paid", money(model, finance?.cashPaid), RestInk)
        }
    } else {
        StaffPulse(model)
    }
    SectionTitle("Activity")
    val recent = model.activity.take(5)
    if (recent.isEmpty()) {
        RestCard { Text("Nothing recorded yet.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
    recent.forEach { ActivityRow(it, model.financeVisible) }
    Text(
        "View all activity",
        color = RestAccent,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, RestBrand, RoundedCornerShape(14.dp))
            .clickable(onClick = onViewAll)
            .padding(vertical = 14.dp),
    )
}

@Composable
private fun StaffPulse(model: OperatingProjectionDto) {
    Pill(roleLabel(model.role))
    RestCard {
        Text("Your view", color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text("Assigned work stays visible. Protected amounts stay hidden.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
    }
    model.attention.forEach { item ->
        RestCard {
            Text(item.title, color = RestInk, fontFamily = PlusJakartaSans, fontSize = 15.sp)
            Text(item.kind, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
    }
    RestCard {
        Text("Finance totals stay with Finance, Approver, and Owner.", color = RestInk, fontFamily = PlusJakartaSans, fontSize = 15.sp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun RestaurantMoments(model: OperatingProjectionDto, onOpenQuickAdd: () -> Unit) {
    Header(model, "Moments", "Orders, stock, and the restaurant this month.")
    RestCard {
        Text(periodLabel(model.startDate), color = RestAccent, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        Text("This month, in motion", color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Sales", money(model, model.finance?.sales ?: model.target?.progress), Modifier.weight(1f))
            Metric("Target", money(model, model.target?.value), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Spent", money(model, model.budget?.spent), Modifier.weight(1f))
            Metric("Remaining", money(model, model.budget?.remaining), Modifier.weight(1f))
        }
        Meter("Sales toward target", fraction(model.target?.progress, model.target?.value), RestBrand)
    }
    listOf("sales", "purchases", "updates", "stock", "issues", "expenses", "payments", "channels").forEach { code ->
        val section = model.boards?.moments?.firstOrNull { it.code == code } ?: return@forEach
        BoardBlock(section, model.financeVisible)
    }
    RestCard {
        Text("Create the next restaurant moment", color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(
            "Open Quick Add",
            color = RestBg,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(RestBrand)
                .clickable(onClick = onOpenQuickAdd)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun RestaurantLife(model: OperatingProjectionDto) {
    Header(model, "Life", "How the restaurant is moving today.")
    (model.boards?.life ?: emptyList()).forEach { BoardBlock(it, model.financeVisible) }
    if (model.boards?.life.isNullOrEmpty()) {
        RestCard { Text("Nothing recorded yet.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp) }
    }
}

@Composable
private fun RestaurantMemory(model: OperatingProjectionDto) {
    Header(model, "Memory", "What this restaurant keeps from the month.")
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
                .background(RestBg)
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("All activity", color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                Text("Close", color = RestSecondary, fontFamily = PlusJakartaSans, modifier = Modifier.clickable(onClick = onClose))
            }
            Text(model.title ?: "Restaurant", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 13.sp)
            if (model.activity.isEmpty()) {
                Text("Nothing recorded yet.", color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            }
            model.activity.forEach { ActivityRow(it, model.financeVisible) }
        }
    }
}

@Composable
private fun Header(model: OperatingProjectionDto, title: String, body: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp)
        Pill(roleLabel(model.role))
    }
    Text(body, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
    Text(snapshot(model.startDate), color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
}

@Composable
private fun BoardBlock(section: OperatingBoardSectionDto, financeVisible: Boolean) {
    SectionTitle(section.title)
    RestCard {
        Text(section.detail.ifBlank { "Nothing recorded yet." }, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        section.rows.forEach { row ->
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(row.title, color = RestInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                val extra = listOfNotNull(row.channelName, row.counterpartyName, row.paymentStatus?.takeIf { it != "NONE" }).joinToString(" · ")
                if (extra.isNotBlank()) Text(extra, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
                if (financeVisible && !row.amount.isNullOrBlank()) {
                    Text(row.amount, color = RestAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(row: OperatingActivityDto, financeVisible: Boolean) {
    RestCard {
        Text(row.eventType.replace('_', ' '), color = RestAccent, fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Text(row.title, color = RestInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        val bits = listOfNotNull(
            row.channelName,
            row.counterpartyName,
            if (financeVisible) row.amount else null,
        )
        if (bits.isNotEmpty()) Text(bits.joinToString(" · "), color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
    }
}

@Composable
private fun ReadinessGroup(label: String, rows: List<com.example.momentra.data.api.OperatingPaymentDto>, model: OperatingProjectionDto) {
    if (rows.isEmpty()) return
    RestCard {
        Text(label, color = RestAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        rows.forEach { item ->
            Line(item.title, money(model, item.amount), RestInk)
            Text(item.status, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
    }
}

@Composable
private fun RestCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(RestCard)
            .border(1.dp, RestBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
}

@Composable
private fun Pill(text: String) {
    Text(
        text,
        color = RestAccent,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(RestNotice)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(RestNotice)
            .padding(12.dp),
    ) {
        Text(label, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Text(value, color = RestInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun Meter(label: String, fraction: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = RestSecondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text("${(fraction * 100).toInt()}%", color = color, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)).background(RestBorder)) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(6.dp).background(color))
        }
    }
}

@Composable
private fun Line(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = RestInk, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        Text(value, color = color, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private fun roleLabel(code: String?): String =
    OperatingMomentCatalog.roles.firstOrNull { it.first == code }?.second ?: "Restaurant"

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
