import SwiftUI

struct RetailStoreSetupView: View {
    let subtypes: [OperatingSubtype]
    let subtype: OperatingSubtype
    let ownerName: String
    @ObservedObject var createModel: MomentCreateModel
    var onSubtype: (OperatingSubtype) -> Void
    var onClose: () -> Void
    var onActivate: ([String: Any], String) -> Void

    @State private var name = ""
    @State private var currency = "INR"
    @State private var currencyOpen = false
    @State private var start = Date()
    @State private var dateOpen = false
    @State private var details = ""
    @State private var location = ""
    @State private var invites: [StoreInvite] = []
    @State private var inviting = false
    @State private var inviteName = ""
    @State private var inviteRole = "OPERATIONS_LEAD"
    @State private var policy = "NONE"
    @State private var policyOpen = false
    @State private var selectedChannels: Set<String> = []
    @State private var budget = ""
    @State private var target = ""
    @State private var localError: String?

    private var spec: SetupSpec { SetupSpec.forCode(subtype.code) }
    private let currencies = [
        ("INR", "INR · Indian Rupee (₹)"),
        ("USD", "USD · US Dollar"),
        ("EUR", "EUR · Euro"),
        ("GBP", "GBP · British Pound"),
        ("AED", "AED · UAE Dirham"),
    ]
    private let policies = [
        ("NONE", "No approval required"),
        ("APPROVAL_REQUIRED", "Approval required"),
    ]
    private var owner: String { PersonLabel.personName(ownerName, fallback: "You") }
    private var needsApprover: Bool { policy == "APPROVAL_REQUIRED" && !invites.contains { $0.role == "APPROVER" } }
    private var ready: Bool {
        !name.trimmingCharacters(in: .whitespaces).isEmpty &&
        !selectedChannels.isEmpty &&
        (Double(budget) ?? 0) > 0 &&
        (Double(target) ?? 0) > 0 &&
        !needsApprover &&
        !createModel.state.submitting
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                header
                hero
                modelSelector
                HStack {
                    Text("3 steps · One simple setup").font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
                    Spacer()
                    Image("rs_setup_arrow_down").resizable().frame(width: 16, height: 16)
                }
                pill(spec.badge, fill: spec.draftFill, ink: spec.draftInk)
                basics
                people
                plan
                review
                if ready { pill("Ready to activate · Still a draft", fill: spec.statusFill, ink: spec.statusInk) }
                Text("Name, currency, Owner and channels checked. Positive Budget and Sales Target validated.")
                    .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
                if let message = localError ?? createModel.state.error {
                    Text(message).font(.plusJakarta(size: 13)).foregroundStyle(Color(hex: "#F87171"))
                }
                Button(createModel.state.submitting ? "Activating…" : spec.activate, action: activate)
                    .font(.plusJakarta(size: 15, weight: .semibold))
                    .foregroundStyle(spec.actionInk.opacity(ready ? 1 : 0.45))
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                    .background(spec.brand)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .disabled(!ready)
                    .accessibilityIdentifier("business.setup.submit")
                Text(spec.footer)
                    .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
            }
            .padding(.horizontal, 17)
            .padding(.vertical, 20)
        }
        .background(spec.bg)
        .onChange(of: subtype.code) { _, _ in
            selectedChannels = selectedChannels.intersection(Set(spec.channels))
        }
        .sheet(isPresented: $dateOpen) {
            VStack {
                DatePicker("Tracking start", selection: $start, displayedComponents: .date)
                    .datePickerStyle(.graphical)
                    .padding()
                Button("Done") { dateOpen = false }.padding(.bottom, 20)
            }
            .presentationDetents([.medium])
        }
    }

    private var header: some View {
        HStack {
            Button(action: onClose) {
                HStack(spacing: 6) {
                    Image("rs_setup_close").resizable().frame(width: 14, height: 14)
                    Text("Close").font(.plusJakarta(size: 14)).foregroundStyle(spec.secondary)
                }
            }
            Spacer()
            Text(spec.mode).font(.plusJakarta(size: 10, weight: .semibold)).foregroundStyle(spec.accent)
        }
    }

    private var hero: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle().fill(spec.notice).frame(width: 88, height: 88)
                    .overlay(Circle().stroke(spec.border, lineWidth: 1))
                Image(spec.hero).resizable().frame(width: 38, height: 38)
            }
            Text(spec.title)
                .font(.plusJakarta(size: 32, weight: .semibold))
                .foregroundStyle(spec.text)
                .multilineTextAlignment(.center)
            Text("Bring your business, people and monthly plan together. Make it yours before you activate.")
                .font(.plusJakarta(size: 14))
                .foregroundStyle(spec.secondary)
                .multilineTextAlignment(.center)
            pill("Setup draft · Not active", fill: spec.draftFill, ink: spec.draftInk)
        }
        .frame(maxWidth: .infinity)
    }

    private var modelSelector: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("OPERATING MODEL").font(.plusJakarta(size: 10, weight: .semibold)).foregroundStyle(spec.secondary)
            HStack(spacing: 6) {
                ForEach(subtypes) { item in
                    let on = item.code == subtype.code
                    Button { onSubtype(item) } label: {
                        VStack(spacing: 7) {
                            Image(modelIcon(item.code)).resizable().frame(width: 20, height: 20)
                            Text(item.label).font(.plusJakarta(size: 10, weight: .semibold)).multilineTextAlignment(.center)
                        }
                        .foregroundStyle(on ? spec.chipOnLabel : spec.chipOffLabel)
                        .frame(maxWidth: .infinity)
                        .frame(height: 80)
                        .background(on ? spec.chipOnFill : spec.chipOffFill)
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(on ? spec.chipOnBorder : spec.border, lineWidth: 1))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                }
            }
        }
        .padding(12)
        .background(spec.card)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(spec.border, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 18))
    }

    private var basics: some View {
        section("01", "Business Basics") {
            Text(spec.basicsLine).font(.plusJakarta(size: 14)).foregroundStyle(spec.secondary)
            editor("Business name · Required", text: $name, placeholder: spec.nameHint, icon: "rs_setup_pencil")
            choice("Currency · Required", currencies.first { $0.0 == currency }?.1 ?? currency, icon: "rs_setup_chevron") { currencyOpen.toggle() }
            if currencyOpen {
                ForEach(currencies, id: \.0) { item in
                    Button(item.1) { currency = item.0; currencyOpen = false }
                        .font(.plusJakarta(size: 14))
                        .foregroundStyle(item.0 == currency ? spec.accent : .white)
                }
            }
            choice("Tracking start", dayLabel(start), icon: "rs_setup_calendar") { dateOpen = true }
            editor("Description · Optional", text: $details, placeholder: spec.descriptionHint, icon: "rs_setup_pencil")
            editor("Location · Optional", text: $location, placeholder: spec.locationHint, icon: "rs_setup_pin")
            Text("Required fields are marked. Refine optional details later.")
                .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
        }
    }

    private var people: some View {
        section("02", "People & Roles") {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(owner) · You").font(.plusJakarta(size: 14)).foregroundStyle(spec.text)
                    Text("Creator · Owner / Admin").font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
                }
                Spacer()
                Text(initials(owner)).font(.plusJakarta(size: 14, weight: .semibold)).foregroundStyle(spec.accent)
            }
            pill("Required Owner assigned", fill: spec.statusFill, ink: spec.statusInk)
            ForEach(invites) { person in
                Text("\(person.name) · \(roleLabel(person.role))").font(.plusJakarta(size: 14)).foregroundStyle(spec.text)
            }
            Button(inviting ? "Close invite" : "+ Invite or add people") { inviting.toggle() }
                .font(.plusJakarta(size: 14, weight: .semibold))
                .foregroundStyle(spec.actionInk)
                .frame(maxWidth: .infinity)
                .padding(14)
                .background(spec.inviteFill)
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(spec.inviteBorder, lineWidth: 1))
                .clipShape(RoundedRectangle(cornerRadius: 14))
            if inviting {
                editor("Name", text: $inviteName, placeholder: "Person name", icon: "rs_setup_pencil")
                ForEach(OperatingMomentCatalog.roles.filter { $0.0 != "OWNER_ADMIN" }, id: \.0) { item in
                    Button(item.1) { inviteRole = item.0 }
                        .font(.plusJakarta(size: 11, weight: .semibold))
                        .foregroundStyle(inviteRole == item.0 ? spec.accent : spec.secondary)
                }
                Button("Add") {
                    let trimmed = inviteName.trimmingCharacters(in: .whitespaces)
                    guard !trimmed.isEmpty else { return }
                    invites.append(StoreInvite(name: trimmed, role: inviteRole))
                    inviteName = ""
                    inviting = false
                }
                .foregroundStyle(spec.text)
            }
            Text(spec.peopleNote)
                .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
            Text("Role & access preview").font(.plusJakarta(size: 16)).foregroundStyle(spec.text)
            ForEach(spec.roles, id: \.0) { row in
                VStack(alignment: .leading, spacing: 4) {
                    Text(row.0).font(.plusJakarta(size: 14)).foregroundStyle(spec.text)
                    Text(row.1).font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
                }
                .padding(.vertical, 8)
            }
            choice("Initial activation policy", policies.first { $0.0 == policy }?.1 ?? policy, icon: "rs_setup_chevron") { policyOpen.toggle() }
            if policyOpen {
                ForEach(policies, id: \.0) { item in
                    Button(item.1) { policy = item.0; policyOpen = false }
                        .font(.plusJakarta(size: 14))
                        .foregroundStyle(item.0 == policy ? spec.accent : .white)
                }
            }
            VStack(alignment: .leading, spacing: 6) {
                Text(needsApprover ? "A checker is required" : "\(owner) alone can activate")
                    .font(.plusJakarta(size: 13, weight: .semibold)).foregroundStyle(spec.accent)
                Text(needsApprover
                     ? "Add a person with the Approver role, or choose no approval required."
                     : "A separate checker is needed only if you enable an activation approval policy. Manager and Finance are not basic activation requirements.")
                    .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(spec.notice)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            Text("Only the Owner changes roles. Actions also depend on scope, policy and the source’s current state — not membership alone.")
                .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
        }
    }

    private var plan: some View {
        section("03", "Channels & Monthly Plan") {
            Text(spec.channelPrompt).font(.plusJakarta(size: 14)).foregroundStyle(spec.text)
            FlowLayout(spacing: 8) {
                ForEach(spec.channels, id: \.self) { channel in
                    let on = selectedChannels.contains(channel)
                    Button(on ? "✓ \(channel)" : channel) {
                        if on { selectedChannels.remove(channel) } else { selectedChannels.insert(channel) }
                    }
                    .font(.plusJakarta(size: on ? 11 : 12, weight: on ? .semibold : .regular))
                    .foregroundStyle(on ? spec.channelOnLabel : spec.channelOffLabel)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(on ? spec.channelOnFill : Color.clear)
                    .overlay(RoundedRectangle(cornerRadius: on ? 999 : 8).stroke(spec.border, lineWidth: on ? 0 : 1))
                    .clipShape(RoundedRectangle(cornerRadius: on ? 999 : 8))
                }
            }
            Text(selectedChannels.isEmpty
                 ? "Choose at least one channel."
                 : "\(selectedChannels.count) channels selected. Channels describe where you sell, not a different business type.")
                .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
            choice("Monthly period", "\(monthLabel(start)) · Plan v1", icon: "rs_setup_calendar") { dateOpen = true }
            editor("Monthly Budget · Required, positive", text: $budget, placeholder: "Amount", icon: "rs_setup_pencil")
            editor("Monthly Sales Target · Required, positive", text: $target, placeholder: "Amount", icon: "rs_setup_pencil")
            VStack(alignment: .leading, spacing: 6) {
                Text("Measured against recorded Sales").font(.plusJakarta(size: 13, weight: .semibold)).foregroundStyle(spec.accent)
                Text("Validated Sales count toward the target, not cash Collected. Eligible Purchase and Expense facts count once toward Budget.")
                    .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(spec.notice)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            if ready { pill("Both plan amounts are positive", fill: spec.statusFill, ink: spec.statusInk) }
        }
    }

    private var review: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Review your setup").font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(spec.text)
            VStack(alignment: .leading, spacing: 4) {
                Text(name.isEmpty ? "Business name" : name).font(.plusJakarta(size: 14)).foregroundStyle(spec.text)
                Text("\(spec.reviewKind) · \(currency) · From \(dayLabel(start))").font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
            }
            reviewLine("People", ([ "\(owner) · Owner" ] + invites.map { "\($0.name) · \(roleLabel($0.role))" }).joined(separator: ", "))
            Text("Channels · \(selectedChannels.sorted().joined(separator: ", ").isEmpty ? "None yet" : selectedChannels.sorted().joined(separator: ", "))")
                .font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
            reviewLine("\(monthLabel(start)) Budget", money(budget))
            reviewLine("\(monthLabel(start)) Sales Target", money(target))
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(spec.notice)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(spec.border, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 18))
    }

    private func section<Content: View>(_ number: String, _ title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 14) {
                Text(number).font(.plusJakarta(size: 48, weight: .heavy)).foregroundStyle(spec.number)
                Text(title).font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(spec.text)
            }
            content()
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(spec.card)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(spec.border, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 18))
    }

    private func editor(_ label: String, text: Binding<String>, placeholder: String, icon: String) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(label).font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
                TextField(placeholder, text: text).font(.plusJakarta(size: 14)).foregroundStyle(spec.text)
            }
            Spacer()
            Image(icon).resizable().frame(width: 16, height: 16)
        }
        .padding(14)
        .background(spec.field)
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(spec.border, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }

    private func choice(_ label: String, _ value: String, icon: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(label).font(.plusJakarta(size: 12)).foregroundStyle(spec.secondary)
                    Text(value).font(.plusJakarta(size: 14)).foregroundStyle(spec.text)
                }
                Spacer()
                Image(icon).resizable().frame(width: 16, height: 16)
            }
            .padding(14)
            .background(spec.field)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(spec.border, lineWidth: 1))
            .clipShape(RoundedRectangle(cornerRadius: 8))
        }
    }

    private func reviewLine(_ label: String, _ value: String) -> some View {
        HStack {
            Text(label).font(.plusJakarta(size: 14)).foregroundStyle(spec.text)
            Spacer()
            Text(value).font(.plusJakarta(size: 14, weight: .semibold)).foregroundStyle(spec.accent)
        }
        .padding(.vertical, 8)
    }

    private func pill(_ text: String, fill: Color, ink: Color) -> some View {
        Text(text)
            .font(.plusJakarta(size: 11, weight: .semibold))
            .foregroundStyle(ink)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(fill)
            .overlay(Capsule().stroke(spec.pillBorder, lineWidth: spec.pillBorder == .clear ? 0 : 1))
            .clipShape(Capsule())
    }

    private func activate() {
        let trimmed = name.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty { localError = "Business name is required."; return }
        if selectedChannels.isEmpty { localError = "Choose at least one channel."; return }
        if (Double(budget) ?? 0) <= 0 { localError = "Monthly budget must be a positive amount."; return }
        if (Double(target) ?? 0) <= 0 { localError = "Monthly target must be a positive amount."; return }
        if needsApprover { localError = "Add an Approver, or choose no approval required."; return }
        localError = nil
        let participants: [[String: String]] = invites.map { ["displayName": $0.name, "role": $0.role] }
        let prefs: [String: Any] = [
            "subtype": spec.code,
            "identityName": trimmed,
            "partyName": trimmed,
            "description": details.trimmingCharacters(in: .whitespaces),
            "currency": currency,
            "startDate": isoDay(start),
            "location": location.trimmingCharacters(in: .whitespaces),
            "activationPolicy": policy,
            "participants": participants,
            "counterparties": [[String: String]](),
            "channels": Array(selectedChannels),
            "monthlyBudget": budget.trimmingCharacters(in: .whitespaces),
            "monthlyTarget": target.trimmingCharacters(in: .whitespaces),
            "targetMetric": "REVENUE",
        ]
        onActivate(prefs, trimmed)
    }

    private func modelIcon(_ code: String) -> String {
        switch code {
        case "RESTAURANT_FOOD": return "rs_setup_utensils"
        case "CONSUMER_SERVICE": return "rs_setup_handshake"
        default: return "rs_setup_store_chip"
        }
    }

    private func roleLabel(_ code: String) -> String {
        OperatingMomentCatalog.roles.first { $0.0 == code }?.1 ?? code
    }

    private func initials(_ name: String) -> String {
        let parts = name.split(separator: " ").prefix(2)
        let letters = parts.compactMap { $0.first }.map { String($0).uppercased() }.joined()
        return letters.isEmpty ? "Y" : letters
    }

    private func money(_ amount: String) -> String {
        let trimmed = amount.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty { return "—" }
        return currency == "INR" ? "₹\(trimmed)" : "\(currency) \(trimmed)"
    }

    private func dayLabel(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "dd MMMM yyyy"
        return formatter.string(from: date)
    }

    private func monthLabel(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "MMMM yyyy"
        return formatter.string(from: date)
    }

    private func isoDay(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }
}

private struct SetupSpec {
    let code: String
    let bg: Color
    let card: Color
    let field: Color
    let border: Color
    let notice: Color
    let brand: Color
    let accent: Color
    let number: Color
    let secondary: Color
    let text: Color
    let statusFill: Color
    let statusInk: Color
    let draftFill: Color
    let draftInk: Color
    let inviteFill: Color
    let inviteBorder: Color
    let actionInk: Color
    let pillBorder: Color
    let chipOnFill: Color
    let chipOnBorder: Color
    let chipOnLabel: Color
    let chipOffFill: Color
    let chipOffLabel: Color
    let channelOnFill: Color
    let channelOnLabel: Color
    let channelOffLabel: Color
    let hero: String
    let mode: String
    let title: String
    let badge: String
    let basicsLine: String
    let nameHint: String
    let descriptionHint: String
    let locationHint: String
    let ownerAccess: String
    let leadTitle: String
    let leadBody: String
    let peopleNote: String
    let channelPrompt: String
    let channels: [String]
    let activate: String
    let footer: String
    let reviewKind: String

    var roles: [(String, String)] {
        [
            ("Owner / Admin", ownerAccess),
            (leadTitle, leadBody),
            ("Finance / Accounts", "Financial records · No approval by default; grants possible"),
            ("Approver", "Assigned checker · not payer by default"),
            ("Contributor", "Contribute facts and evidence in assigned scope"),
            ("Team Member", "Assigned work updates · protected amounts hidden"),
            ("Observer", "Read-only scoped sources · no protected financial values"),
        ]
    }

    static func forCode(_ code: String) -> SetupSpec {
        switch code {
        case "RESTAURANT_FOOD":
            return SetupSpec(
                code: code,
                bg: Color(hex: "#101412"), card: Color(hex: "#202A24"), field: Color(hex: "#1A211D"),
                border: Color(hex: "#344238"), notice: Color(hex: "#202A24"), brand: Color(hex: "#F2B544"),
                accent: Color(hex: "#F2B544"), number: Color(hex: "#F2B544"), secondary: Color(hex: "#B9C5BA"),
                text: Color(hex: "#F4F3EB"), statusFill: Color(hex: "#202A24"), statusInk: Color(hex: "#F2B544"),
                draftFill: Color(hex: "#202A24"), draftInk: Color(hex: "#F2B544"),
                inviteFill: Color(hex: "#F2B544"), inviteBorder: Color(hex: "#F2B544"), actionInk: Color(hex: "#101412"),
                pillBorder: Color(hex: "#344238"),
                chipOnFill: Color(hex: "#202A24"), chipOnBorder: Color(hex: "#F2B544"), chipOnLabel: Color(hex: "#F4F3EB"),
                chipOffFill: Color(hex: "#1A211D"), chipOffLabel: Color(hex: "#B9C5BA"),
                channelOnFill: Color(hex: "#F2B544"), channelOnLabel: Color(hex: "#101412"), channelOffLabel: Color(hex: "#F4F3EB"),
                hero: "rs_setup_restaurant", mode: "RESTAURANT MODE", title: "Restaurant / Food Setup",
                badge: "Restaurant / Food · Setup draft", basicsLine: "A home for this restaurant.",
                nameHint: "Restaurant name", descriptionHint: "Everyday notes about this restaurant",
                locationHint: "Add your restaurant location",
                ownerAccess: "Full restaurant access · only Owner changes roles",
                leadTitle: "Restaurant Manager", leadBody: "Operations Lead · no full Finance by default",
                peopleNote: "Start on your own. Managers, Finance and other roles are optional; invitations can follow activation. Suppliers are added through Quick Adds after activation.",
                channelPrompt: "Where do you sell? · Choose at least one",
                channels: ["Dine-in", "Direct", "Zomato", "Takeaway", "Swiggy"],
                activate: "Activate Restaurant Moment",
                footer: "Private to your business. Only the Owner can change roles and access. No activity counts until the restaurant is active.",
                reviewKind: "Restaurant / Food"
            )
        case "CONSUMER_SERVICE":
            return SetupSpec(
                code: code,
                bg: Color(hex: "#080F0E"), card: Color(hex: "#0F2320"), field: Color(hex: "#0B1917"),
                border: Color(hex: "#1E3530"), notice: Color(hex: "#0F2320"), brand: Color(hex: "#0ECBB0"),
                accent: Color(hex: "#0ECBB0"), number: Color(hex: "#0ECBB0"), secondary: Color(hex: "#8FBFBA"),
                text: Color(hex: "#D8F0ED"), statusFill: Color(hex: "#0F2320"), statusInk: Color(hex: "#D8F0ED"),
                draftFill: Color(hex: "#0F2320"), draftInk: Color(hex: "#D8F0ED"),
                inviteFill: Color(hex: "#0ECBB0"), inviteBorder: Color(hex: "#0ECBB0"), actionInk: Color(hex: "#080F0E"),
                pillBorder: Color(hex: "#1E3530"),
                chipOnFill: Color(hex: "#0ECBB0"), chipOnBorder: Color(hex: "#0ECBB0"), chipOnLabel: Color(hex: "#080F0E"),
                chipOffFill: Color(hex: "#0B1917"), chipOffLabel: Color(hex: "#8FBFBA"),
                channelOnFill: Color(hex: "#0ECBB0"), channelOnLabel: Color(hex: "#080F0E"), channelOffLabel: Color(hex: "#8FBFBA"),
                hero: "rs_setup_service", mode: "SERVICE MODE", title: "Consumer Service Setup",
                badge: "Consumer Service · Setup draft", basicsLine: "A home for this service.",
                nameHint: "Service name", descriptionHint: "Everyday notes about this service",
                locationHint: "Add your service location",
                ownerAccess: "Full service business access · only Owner changes roles",
                leadTitle: "Service Manager", leadBody: "Operations Lead · no full Finance by default",
                peopleNote: "Start on your own. Managers, Finance and other roles are optional; invitations can follow activation.",
                channelPrompt: "How do customers reach you? · Choose at least one",
                channels: ["Walk-in", "Direct booking", "Platform", "Home service"],
                activate: "Activate Service Moment",
                footer: "Private to your business. Only the Owner can change roles and access. No activity counts until the service business is active.",
                reviewKind: "Consumer Service"
            )
        default:
            return SetupSpec(
                code: "RETAIL_STORE",
                bg: Color(hex: "#0B1020"), card: Color(hex: "#161B2E"), field: Color(hex: "#0B1020"),
                border: Color(hex: "#2D3554"), notice: Color(hex: "#252654"), brand: Color(hex: "#5B5CEB"),
                accent: Color(hex: "#A5A7FF"), number: Color(hex: "#565A91"), secondary: Color(hex: "#A1A7C1"),
                text: .white, statusFill: Color(hex: "#102C30"), statusInk: Color(hex: "#10B981"),
                draftFill: Color(hex: "#252654"), draftInk: Color(hex: "#A5A7FF"),
                inviteFill: Color(hex: "#1E243D"), inviteBorder: Color(hex: "#2D3554"), actionInk: .white,
                pillBorder: .clear,
                chipOnFill: Color(hex: "#252654"), chipOnBorder: Color(hex: "#5B5CEB"), chipOnLabel: Color(hex: "#A5A7FF"),
                chipOffFill: Color(hex: "#1A2036"), chipOffLabel: Color(hex: "#A1A7C1"),
                channelOnFill: Color(hex: "#252654"), channelOnLabel: Color(hex: "#A5A7FF"), channelOffLabel: Color(hex: "#A1A7C1"),
                hero: "rs_setup_store", mode: "BUSINESS MODE", title: "Retail Store Setup",
                badge: "Retail Store · Setup draft", basicsLine: "A home for this store.",
                nameHint: "Store name", descriptionHint: "Everyday notes about this store",
                locationHint: "Add your store location",
                ownerAccess: "Full store access · only Owner changes roles",
                leadTitle: "Store Manager", leadBody: "Operations Lead · no full Finance by default",
                peopleNote: "Start on your own. Managers, Finance and other roles are optional; invitations can follow activation.",
                channelPrompt: "Where do you sell? · Choose at least one",
                channels: ["Store", "Website", "Marketplace", "Social", "Direct"],
                activate: "Activate Retail Moment",
                footer: "Private to your business. Only the Owner can change roles and access. No activity counts until the store is active.",
                reviewKind: "Retail Store"
            )
        }
    }
}

private struct StoreInvite: Identifiable {
    let id = UUID()
    let name: String
    let role: String
}

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? 0
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > width && x > 0 {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            rowHeight = max(rowHeight, size.height)
            x += size.width + spacing
        }
        return CGSize(width: width, height: y + rowHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0
        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX && x > bounds.minX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            view.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            rowHeight = max(rowHeight, size.height)
            x += size.width + spacing
        }
    }
}
