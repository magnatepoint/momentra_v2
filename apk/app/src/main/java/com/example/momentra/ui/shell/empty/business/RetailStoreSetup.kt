package com.example.momentra.ui.shell.empty.business

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.example.momentra.R
import com.example.momentra.ui.setup.SetupDateTimeUtils
import com.example.momentra.ui.shell.business.shared.OperatingMomentCatalog
import com.example.momentra.ui.shell.business.shared.OperatingSubtype
import com.example.momentra.ui.shell.maestro.MaestroIds
import androidx.compose.ui.platform.testTag
import com.example.momentra.ui.shell.shared.PersonLabel
import com.example.momentra.ui.theme.PlusJakartaSans
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class SetupLook(
    val code: String,
    val bg: Color,
    val card: Color,
    val field: Color,
    val border: Color,
    val notice: Color,
    val brand: Color,
    val accent: Color,
    val number: Color,
    val secondary: Color,
    val text: Color,
    val statusFill: Color,
    val statusInk: Color,
    val draftFill: Color,
    val draftInk: Color,
    val inviteFill: Color,
    val actionInk: Color,
    val activateFill: Color,
    val pillBorder: Color,
    val chipOnFill: Color,
    val chipOnBorder: Color,
    val chipOnLabel: Color,
    val chipOffFill: Color,
    val chipOffLabel: Color,
    val channelOnFill: Color,
    val channelOnLabel: Color,
    val channelOffLabel: Color,
    val hero: Int,
    val mode: String,
    val title: String,
    val badge: String,
    val basicsLine: String,
    val nameHint: String,
    val descriptionHint: String,
    val locationHint: String,
    val ownerAccess: String,
    val leadTitle: String,
    val leadBody: String,
    val peopleNote: String,
    val channelPrompt: String,
    val channels: List<String>,
    val activate: String,
    val footer: String,
    val reviewKind: String,
)

private val sharedRoles = listOf(
    "Finance / Accounts" to "Financial records · No approval by default; grants possible",
    "Approver" to "Assigned checker · not payer by default",
    "Contributor" to "Contribute facts and evidence in assigned scope",
    "Team Member" to "Assigned work updates · protected amounts hidden",
    "Observer" to "Read-only scoped sources · no protected financial values",
)

private fun setupLook(code: String): SetupLook = when (code) {
    "RESTAURANT_FOOD" -> SetupLook(
        code = code,
        bg = Color(0xFF101412),
        card = Color(0xFF202A24),
        field = Color(0xFF1A211D),
        border = Color(0xFF344238),
        notice = Color(0xFF202A24),
        brand = Color(0xFFF2B544),
        accent = Color(0xFFF2B544),
        number = Color(0xFFF2B544),
        secondary = Color(0xFFB9C5BA),
        text = Color(0xFFF4F3EB),
        statusFill = Color(0xFF202A24),
        statusInk = Color(0xFFF2B544),
        draftFill = Color(0xFF202A24),
        draftInk = Color(0xFFF2B544),
        inviteFill = Color(0xFFF2B544),
        actionInk = Color(0xFF101412),
        activateFill = Color(0xFFF2B544),
        pillBorder = Color(0xFF344238),
        chipOnFill = Color(0xFF202A24),
        chipOnBorder = Color(0xFFF2B544),
        chipOnLabel = Color(0xFFF4F3EB),
        chipOffFill = Color(0xFF1A211D),
        chipOffLabel = Color(0xFFB9C5BA),
        channelOnFill = Color(0xFFF2B544),
        channelOnLabel = Color(0xFF101412),
        channelOffLabel = Color(0xFFF4F3EB),
        hero = R.drawable.rs_setup_restaurant,
        mode = "RESTAURANT MODE",
        title = "Restaurant / Food Setup",
        badge = "Restaurant / Food · Setup draft",
        basicsLine = "A home for this restaurant.",
        nameHint = "Restaurant name",
        descriptionHint = "Everyday notes about this restaurant",
        locationHint = "Add your restaurant location",
        ownerAccess = "Full restaurant access · only Owner changes roles",
        leadTitle = "Restaurant Manager",
        leadBody = "Operations Lead · no full Finance by default",
        peopleNote = "Start on your own. Managers, Finance and other roles are optional; invitations can follow activation. Suppliers are added through Quick Adds after activation.",
        channelPrompt = "Where do you sell? · Choose at least one",
        channels = listOf("Dine-in", "Direct", "Zomato", "Takeaway", "Swiggy"),
        activate = "Activate Restaurant Moment",
        footer = "Private to your business. Only the Owner can change roles and access. No activity counts until the restaurant is active.",
        reviewKind = "Restaurant / Food",
    )
    "CONSUMER_SERVICE" -> SetupLook(
        code = code,
        bg = Color(0xFF080F0E),
        card = Color(0xFF0F2320),
        field = Color(0xFF0B1917),
        border = Color(0xFF1E3530),
        notice = Color(0xFF0F2320),
        brand = Color(0xFF0ECBB0),
        accent = Color(0xFF0ECBB0),
        number = Color(0xFF0ECBB0),
        secondary = Color(0xFF8FBFBA),
        text = Color(0xFFD8F0ED),
        statusFill = Color(0xFF0F2320),
        statusInk = Color(0xFFD8F0ED),
        draftFill = Color(0xFF0F2320),
        draftInk = Color(0xFFD8F0ED),
        inviteFill = Color(0xFF0ECBB0),
        actionInk = Color(0xFF080F0E),
        activateFill = Color(0xFF0ECBB0),
        pillBorder = Color(0xFF1E3530),
        chipOnFill = Color(0xFF0ECBB0),
        chipOnBorder = Color(0xFF0ECBB0),
        chipOnLabel = Color(0xFF080F0E),
        chipOffFill = Color(0xFF0B1917),
        chipOffLabel = Color(0xFF8FBFBA),
        channelOnFill = Color(0xFF0ECBB0),
        channelOnLabel = Color(0xFF080F0E),
        channelOffLabel = Color(0xFF8FBFBA),
        hero = R.drawable.rs_setup_service,
        mode = "SERVICE MODE",
        title = "Consumer Service Setup",
        badge = "Consumer Service · Setup draft",
        basicsLine = "A home for this service.",
        nameHint = "Service name",
        descriptionHint = "Everyday notes about this service",
        locationHint = "Add your service location",
        ownerAccess = "Full service business access · only Owner changes roles",
        leadTitle = "Service Manager",
        leadBody = "Operations Lead · no full Finance by default",
        peopleNote = "Start on your own. Managers, Finance and other roles are optional; invitations can follow activation.",
        channelPrompt = "How do customers reach you? · Choose at least one",
        channels = listOf("Walk-in", "Direct booking", "Platform", "Home service"),
        activate = "Activate Service Moment",
        footer = "Private to your business. Only the Owner can change roles and access. No activity counts until the service business is active.",
        reviewKind = "Consumer Service",
    )
    else -> SetupLook(
        code = "RETAIL_STORE",
        bg = Color(0xFF0B1020),
        card = Color(0xFF161B2E),
        field = Color(0xFF0B1020),
        border = Color(0xFF2D3554),
        notice = Color(0xFF252654),
        brand = Color(0xFF5B5CEB),
        accent = Color(0xFFA5A7FF),
        number = Color(0xFF565A91),
        secondary = Color(0xFFA1A7C1),
        text = Color.White,
        statusFill = Color(0xFF102C30),
        statusInk = Color(0xFF10B981),
        draftFill = Color(0xFF252654),
        draftInk = Color(0xFFA5A7FF),
        inviteFill = Color(0xFF1E243D),
        actionInk = Color.White,
        activateFill = Color(0xFF5B5CEB),
        pillBorder = Color.Transparent,
        chipOnFill = Color(0xFF252654),
        chipOnBorder = Color(0xFF5B5CEB),
        chipOnLabel = Color(0xFFA5A7FF),
        chipOffFill = Color(0xFF1A2036),
        chipOffLabel = Color(0xFFA1A7C1),
        channelOnFill = Color(0xFF252654),
        channelOnLabel = Color(0xFFA5A7FF),
        channelOffLabel = Color(0xFFA1A7C1),
        hero = R.drawable.rs_setup_store,
        mode = "BUSINESS MODE",
        title = "Retail Store Setup",
        badge = "Retail Store · Setup draft",
        basicsLine = "A home for this store.",
        nameHint = "Store name",
        descriptionHint = "Everyday notes about this store",
        locationHint = "Add your store location",
        ownerAccess = "Full store access · only Owner changes roles",
        leadTitle = "Store Manager",
        leadBody = "Operations Lead · no full Finance by default",
        peopleNote = "Start on your own. Managers, Finance and other roles are optional; invitations can follow activation.",
        channelPrompt = "Where do you sell? · Choose at least one",
        channels = listOf("Store", "Website", "Marketplace", "Social", "Direct"),
        activate = "Activate Retail Moment",
        footer = "Private to your business. Only the Owner can change roles and access. No activity counts until the store is active.",
        reviewKind = "Retail Store",
    )
}

private val LocalLook = staticCompositionLocalOf { setupLook("RETAIL_STORE") }

private val Bg: Color @Composable get() = LocalLook.current.bg
private val Card: Color @Composable get() = LocalLook.current.card
private val Border: Color @Composable get() = LocalLook.current.border
private val Selected: Color @Composable get() = LocalLook.current.notice
private val Brand: Color @Composable get() = LocalLook.current.brand
private val Accent: Color @Composable get() = LocalLook.current.accent
private val Number: Color @Composable get() = LocalLook.current.number
private val Secondary: Color @Composable get() = LocalLook.current.secondary
private val Success: Color @Composable get() = LocalLook.current.statusInk
private val SuccessBg: Color @Composable get() = LocalLook.current.statusFill
private val Elevated: Color @Composable get() = LocalLook.current.inviteFill
private val Ink: Color @Composable get() = LocalLook.current.text
private val Field: Color @Composable get() = LocalLook.current.field
private val DraftFill: Color @Composable get() = LocalLook.current.draftFill
private val DraftInk: Color @Composable get() = LocalLook.current.draftInk

private val channels: List<String> @Composable get() = LocalLook.current.channels
private val roleCopy: List<Pair<String, String>>
    @Composable get() {
        val look = LocalLook.current
        return listOf(
            "Owner / Admin" to look.ownerAccess,
            look.leadTitle to look.leadBody,
        ) + sharedRoles
    }
private val currencies = listOf(
    "INR" to "INR · Indian Rupee (₹)",
    "USD" to "USD · US Dollar",
    "EUR" to "EUR · Euro",
    "GBP" to "GBP · British Pound",
    "AED" to "AED · UAE Dirham",
)
private val policies = listOf(
    "NONE" to "No approval required",
    "APPROVAL_REQUIRED" to "Approval required",
)
private val dayFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
private val monthFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

private data class StoreInvite(val name: String, val role: String)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RetailStoreSetup(
    subtypes: List<OperatingSubtype>,
    subtype: OperatingSubtype,
    onSubtype: (OperatingSubtype) -> Unit,
    ownerName: String,
    submitting: Boolean,
    error: String?,
    onClose: () -> Unit,
    onActivate: (Map<String, Any>, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val owner = PersonLabel.personName(ownerName, fallback = "You")
    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("INR") }
    var currencyOpen by remember { mutableStateOf(false) }
    var start by remember { mutableStateOf(LocalDate.now()) }
    var dateOpen by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var invites by remember { mutableStateOf(listOf<StoreInvite>()) }
    var inviting by remember { mutableStateOf(false) }
    var inviteName by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf("OPERATIONS_LEAD") }
    var policy by remember { mutableStateOf("NONE") }
    var policyOpen by remember { mutableStateOf(false) }
    var selectedChannels by remember { mutableStateOf(setOf<String>()) }
    var budget by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val needsApprover = policy == "APPROVAL_REQUIRED" && invites.none { it.role == "APPROVER" }
    val ready = name.isNotBlank() &&
        selectedChannels.isNotEmpty() &&
        budget.toDoubleOrNull()?.let { it > 0 } == true &&
        target.toDoubleOrNull()?.let { it > 0 } == true &&
        !needsApprover
    val look = setupLook(subtype.code)
    LaunchedEffect(subtype.code) {
        selectedChannels = selectedChannels.intersect(look.channels.toSet())
    }

    CompositionLocalProvider(LocalLook provides look) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onClose)) {
                SetupIcon(R.drawable.rs_setup_close, 14.dp)
                Text("Close", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 14.sp, modifier = Modifier.padding(start = 6.dp))
            }
            Text(look.mode, color = Accent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BoxEmblem()
            Text(look.title, color = Ink, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, textAlign = TextAlign.Center)
            Text(
                "Bring your business, people and monthly plan together. Make it yours before you activate.",
                color = Secondary,
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )
            Pill("Setup draft · Not active", DraftFill, DraftInk)
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).border(1.dp, Border, RoundedCornerShape(18.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("OPERATING MODEL", color = Secondary, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                subtypes.forEach { item ->
                    val on = item.code == subtype.code
                    Column(
                        Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (on) look.chipOnFill else look.chipOffFill)
                            .border(1.dp, if (on) look.chipOnBorder else Border, RoundedCornerShape(10.dp))
                            .clickable { onSubtype(item) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically),
                    ) {
                        SetupIcon(modelIcon(item.code), 20.dp)
                        Text(item.label, color = if (on) look.chipOnLabel else look.chipOffLabel, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("3 steps · One simple setup", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            SetupIcon(R.drawable.rs_setup_arrow_down, 16.dp)
        }
        Pill(look.badge, DraftFill, DraftInk)
        SectionCard("01", "Business Basics") {
            Text(look.basicsLine, color = Secondary, fontFamily = PlusJakartaSans, fontSize = 14.sp, lineHeight = 22.sp)
            FieldEditor("Business name · Required", name, look.nameHint, R.drawable.rs_setup_pencil) { name = it }
            ChoiceRow("Currency · Required", currencies.firstOrNull { it.first == currency }?.second ?: currency, R.drawable.rs_setup_chevron) { currencyOpen = !currencyOpen }
            if (currencyOpen) {
                currencies.forEach { (code, label) ->
                    Text(label, color = if (code == currency) Accent else Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp, modifier = Modifier.clickable { currency = code; currencyOpen = false }.padding(vertical = 6.dp))
                }
            }
            ChoiceRow("Tracking start", start.format(dayFormat), R.drawable.rs_setup_calendar) { dateOpen = true }
            FieldEditor("Description · Optional", description, look.descriptionHint, R.drawable.rs_setup_pencil) { description = it }
            FieldEditor("Location · Optional", location, look.locationHint, R.drawable.rs_setup_pin) { location = it }
            Text("Required fields are marked. Refine optional details later.", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp, lineHeight = 18.sp)
        }
        SectionCard("02", "People & Roles") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("$owner · You", color = Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp, lineHeight = 22.sp)
                    Text("Creator · Owner / Admin", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
                }
                Text(initials(owner), color = Accent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Pill("Required Owner assigned", SuccessBg, Success)
            invites.forEach { person ->
                Text("${person.name} · ${roleLabel(person.role)}", color = Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp)
            }
            Text(
                "+ Invite or add people",
                color = look.actionInk,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Elevated).border(1.dp, if (look.inviteFill == look.activateFill) look.inviteFill else Border, RoundedCornerShape(14.dp)).clickable { inviting = !inviting }.padding(14.dp),
            )
            if (inviting) {
                FieldEditor("Name", inviteName, "Person name", R.drawable.rs_setup_pencil) { inviteName = it }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OperatingMomentCatalog.roles.filter { it.first != "OWNER_ADMIN" }.forEach { (code, label) ->
                        val on = code == inviteRole
                        Text(label, color = if (on) Accent else Secondary, fontFamily = PlusJakartaSans, fontSize = 11.sp, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (on) Selected else Color.Transparent).border(1.dp, if (on) Brand else Border, RoundedCornerShape(999.dp)).clickable { inviteRole = code }.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
                Text("Add", color = Ink, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable {
                    val trimmed = inviteName.trim()
                    if (trimmed.isNotEmpty()) {
                        invites = invites + StoreInvite(trimmed, inviteRole)
                        inviteName = ""
                        inviting = false
                    }
                })
            }
            Text(look.peopleNote, color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp, lineHeight = 18.sp)
            Text("Role & access preview", color = Ink, fontFamily = PlusJakartaSans, fontSize = 16.sp, lineHeight = 24.sp)
            roleCopy.forEach { (title, body) ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(title, color = Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                    Text(body, color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
                }
            }
            ChoiceRow("Initial activation policy", policies.first { it.first == policy }.second, R.drawable.rs_setup_chevron) { policyOpen = !policyOpen }
            if (policyOpen) {
                policies.forEach { (code, label) ->
                    Text(label, color = if (code == policy) Accent else Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp, modifier = Modifier.clickable { policy = code; policyOpen = false }.padding(vertical = 6.dp))
                }
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Selected).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (needsApprover) "A checker is required" else "$owner alone can activate",
                    color = Accent,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
                Text(
                    if (needsApprover) "Add a person with the Approver role, or choose no approval required."
                    else "A separate checker is needed only if you enable an activation approval policy. Manager and Finance are not basic activation requirements.",
                    color = Secondary,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
            }
            Text("Only the Owner changes roles. Actions also depend on scope, policy and the source’s current state — not membership alone.", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp, lineHeight = 18.sp)
        }
        SectionCard("03", "Channels & Monthly Plan") {
            Text(look.channelPrompt, color = Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp, lineHeight = 22.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                channels.forEach { channel ->
                    val on = channel in selectedChannels
                    Text(
                        if (on) "✓ $channel" else channel,
                        color = if (on) look.channelOnLabel else look.channelOffLabel,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = if (on) 11.sp else 12.sp,
                        modifier = Modifier.clip(RoundedCornerShape(if (on) 999.dp else 8.dp)).background(if (on) look.channelOnFill else Color.Transparent).border(if (on) 0.dp else 1.dp, Border, RoundedCornerShape(if (on) 999.dp else 8.dp)).clickable {
                            selectedChannels = if (on) selectedChannels - channel else selectedChannels + channel
                        }.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
            Text(
                if (selectedChannels.isEmpty()) "Choose at least one channel." else "${selectedChannels.size} channels selected. Channels describe where you sell, not a different business type.",
                color = Secondary,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
            )
            ChoiceRow("Monthly period", "${start.format(monthFormat)} · Plan v1", R.drawable.rs_setup_calendar) { dateOpen = true }
            FieldEditor("Monthly Budget · Required, positive", budget, "Amount", R.drawable.rs_setup_pencil) { budget = it.filter { ch -> ch.isDigit() || ch == '.' } }
            FieldEditor("Monthly Sales Target · Required, positive", target, "Amount", R.drawable.rs_setup_pencil) { target = it.filter { ch -> ch.isDigit() || ch == '.' } }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Selected).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Measured against recorded Sales", color = Accent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Validated Sales count toward the target, not cash Collected. Eligible Purchase and Expense facts count once toward Budget.", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp, lineHeight = 18.sp)
            }
            if (ready) Pill("Both plan amounts are positive", SuccessBg, Success)
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Selected).border(1.dp, Border, RoundedCornerShape(18.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Review your setup", color = Ink, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp)
            Column {
                Text(name.ifBlank { "Business name" }, color = Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                Text("${look.reviewKind} · $currency · From ${start.format(dayFormat)}", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            }
            ReviewLine("People", (listOf("$owner · Owner") + invites.map { "${it.name} · ${roleLabel(it.role)}" }).joinToString(", "))
            Text("Channels · ${selectedChannels.joinToString().ifBlank { "None yet" }}", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            ReviewLine("${start.format(monthFormat)} Budget", money(currency, budget))
            ReviewLine("${start.format(monthFormat)} Sales Target", money(currency, target))
        }
        if (ready) Pill("Ready to activate · Still a draft", SuccessBg, Success)
        Text("Name, currency, Owner and channels checked. Positive Budget and Sales Target validated.", color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        val shown = localError ?: error
        if (!shown.isNullOrBlank()) Text(shown, color = Color(0xFFF87171), fontFamily = PlusJakartaSans, fontSize = 13.sp)
        Text(
            if (submitting) "Activating…" else look.activate,
            color = look.actionInk.copy(alpha = if (ready && !submitting) 1f else 0.45f),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .wrapContentHeight(Alignment.CenterVertically)
                .clip(RoundedCornerShape(14.dp))
                .background(look.activateFill)
                .testTag(MaestroIds.BUSINESS_SETUP_SUBMIT)
                .clickable(enabled = ready && !submitting) {
                    val problem = when {
                        name.isBlank() -> "Business name is required."
                        selectedChannels.isEmpty() -> "Choose at least one channel."
                        budget.toDoubleOrNull()?.let { it > 0 } != true -> "Monthly budget must be a positive amount."
                        target.toDoubleOrNull()?.let { it > 0 } != true -> "Monthly target must be a positive amount."
                        needsApprover -> "Add an Approver, or choose no approval required."
                        else -> null
                    }
                    localError = problem
                    if (problem != null) return@clickable
                    val trimmed = name.trim()
                    onActivate(
                        mapOf(
                            "subtype" to look.code,
                            "identityName" to trimmed,
                            "partyName" to trimmed,
                            "description" to description.trim(),
                            "currency" to currency,
                            "startDate" to start.toString(),
                            "location" to location.trim(),
                            "activationPolicy" to policy,
                            "participants" to invites.map { mapOf("displayName" to it.name, "role" to it.role) },
                            "counterparties" to emptyList<Map<String, String>>(),
                            "channels" to selectedChannels.toList(),
                            "monthlyBudget" to budget.trim(),
                            "monthlyTarget" to target.trim(),
                            "targetMetric" to "REVENUE",
                        ),
                        trimmed,
                    )
                },
        )
        Text(look.footer, color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp, lineHeight = 18.sp)
    }

    if (dateOpen) {
        val state = rememberDatePickerState(initialSelectedDateMillis = SetupDateTimeUtils.localDateToMillis(start))
        DatePickerDialog(
            onDismissRequest = { dateOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { start = SetupDateTimeUtils.millisToLocalDate(it) }
                    dateOpen = false
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { dateOpen = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
    }
}

@Composable
private fun BoxEmblem() {
    Column(
        Modifier.size(88.dp).clip(RoundedCornerShape(44.dp)).background(Selected).border(1.dp, Border, RoundedCornerShape(44.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { SetupIcon(LocalLook.current.hero, 38.dp) }
}

@Composable
private fun SectionCard(number: String, title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).border(1.dp, Border, RoundedCornerShape(18.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(number, color = Number, fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp)
            Text(title, color = Ink, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp)
        }
        content()
    }
}

@Composable
private fun FieldRow(label: String, value: String, filled: Boolean, icon: Int, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Field).border(1.dp, Border, RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text(value, color = if (filled) Ink else Secondary, fontFamily = PlusJakartaSans, fontSize = 14.sp, lineHeight = 22.sp)
        }
        SetupIcon(icon, 16.dp)
    }
}

@Composable
private fun ChoiceRow(label: String, value: String, icon: Int, onClick: () -> Unit) {
    FieldRow(label, value, true, icon, onClick)
}

@Composable
private fun FieldEditor(label: String, value: String, placeholder: String, icon: Int, onValue: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Field).border(1.dp, Border, RoundedCornerShape(8.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = Secondary, fontFamily = PlusJakartaSans, fontSize = 12.sp)
            BasicTextField(
                value = value,
                onValueChange = onValue,
                textStyle = TextStyle(color = Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp),
                cursorBrush = SolidColor(Brand),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, color = Secondary, fontFamily = PlusJakartaSans, fontSize = 14.sp)
                    inner()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        SetupIcon(icon, 16.dp)
    }
}

@Composable
private fun ReviewLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Ink, fontFamily = PlusJakartaSans, fontSize = 14.sp)
        Text(value, color = Accent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun Pill(text: String, background: Color, foreground: Color) {
    val stroke = LocalLook.current.pillBorder
    Text(
        text,
        color = foreground,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(if (stroke.alpha == 0f) 0.dp else 1.dp, stroke, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun SetupIcon(res: Int, size: Dp) {
    Image(painter = painterResource(res), contentDescription = null, modifier = Modifier.size(size))
}

private fun modelIcon(code: String): Int = when (code) {
    "RESTAURANT_FOOD" -> R.drawable.rs_setup_utensils
    "CONSUMER_SERVICE" -> R.drawable.rs_setup_handshake
    else -> R.drawable.rs_setup_store_chip
}

private fun initials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    val letters = parts.take(2).map { it.first().uppercaseChar() }.joinToString("")
    return letters.ifBlank { "Y" }
}

private fun roleLabel(code: String): String = OperatingMomentCatalog.roles.firstOrNull { it.first == code }?.second ?: code

private fun money(currency: String, amount: String): String {
    if (amount.isBlank()) return "—"
    return if (currency == "INR") "₹$amount" else "$currency $amount"
}
