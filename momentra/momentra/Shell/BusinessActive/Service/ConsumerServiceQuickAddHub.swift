import SwiftUI

struct ServiceQuickAddHub: View {
    let model: OperatingProjectionPayload
    var onRecord: (ServiceEventDraft) async throws -> Void
    var onDecide: (String, String) async throws -> Void

    @State private var query = ""
    @State private var sheet: ServiceSheetKind?
    @State private var approvalOpen = false
    @State private var errorText: String?

    private var allowed: Set<String> { Set((model.quickAdds ?? []).map(\.code)) }
    private var owner: Bool { model.role == "OWNER_ADMIN" }
    private var approver: Bool { model.role == "APPROVER" || owner }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Quick Adds").font(.plusJakarta(size: 28, weight: .semibold)).foregroundStyle(ServiceChrome.ink)
            Text("Record what happened · \(model.title ?? "")").font(.plusJakarta(size: 13)).foregroundStyle(ServiceChrome.secondary)
            HStack {
                chip(OperatingMomentCatalog.roles.first { $0.0 == model.role }?.1 ?? "Service", filled: true)
                chip("Service", filled: false)
            }
            VStack(alignment: .leading, spacing: 8) {
                Text("Every service moment counts.").font(.plusJakarta(size: 18, weight: .semibold)).foregroundStyle(ServiceChrome.ink)
                Text("Capture a service, a cost, or a note while it is still clear.")
                    .font(.plusJakarta(size: 13)).foregroundStyle(ServiceChrome.secondary)
                if allowed.contains("SERVICE") {
                    Button { sheet = .service } label: {
                        HStack(spacing: 6) {
                            Image("qa_plus").renderingMode(.template).resizable().scaledToFit().frame(width: 14, height: 14)
                            Text("Record Service")
                        }
                    }
                    .font(.plusJakarta(size: 14, weight: .semibold))
                    .foregroundStyle(ServiceChrome.bg)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(ServiceChrome.brand)
                    .clipShape(Capsule())
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(ServiceChrome.notice)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            HStack(spacing: 10) {
                Image("qa_search").renderingMode(.template).resizable().scaledToFit().frame(width: 18, height: 18)
                    .foregroundStyle(ServiceChrome.secondary)
                TextField("Search actions", text: $query).font(.plusJakarta(size: 14))
            }
            .padding(14)
            .background(ServiceChrome.field)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            let core = ServiceSheetKind.core.filter { allowed.contains($0.eventType) && matches($0.label) }
            Text("Core actions · \(core.count)").font(.plusJakarta(size: 12)).foregroundStyle(ServiceChrome.secondary)
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                ForEach(core) { kind in
                    Button { sheet = kind } label: {
                        VStack(spacing: 8) {
                            Image(kind.icon).renderingMode(.template).resizable().scaledToFit().frame(width: 28, height: 28)
                            Text(kind.label).font(.plusJakarta(size: 13, weight: .semibold)).multilineTextAlignment(.center)
                        }
                        .foregroundStyle(ServiceChrome.ink)
                        .frame(maxWidth: .infinity, minHeight: 104)
                        .padding(12)
                        .background(ServiceChrome.card)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(ServiceChrome.border, lineWidth: 1))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                    }
                }
            }
            Text("Support & management").font(.plusJakarta(size: 12)).foregroundStyle(ServiceChrome.secondary)
            supportGrid
            if let errorText { Text(errorText).foregroundStyle(Color(hex: "#F87171")) }
        }
        .sheet(item: $sheet) { kind in
            ServiceEventSheet(kind: kind, model: model) { draft in
                try await onRecord(draft)
            }
        }
        .sheet(isPresented: $approvalOpen) {
            ApprovalList(model: model, onDecide: onDecide)
        }
    }

    private var supportGrid: some View {
        let items: [(String, ServiceSheetKind?, String?)] = [
            ("Reimbursement", .reimburse, allowed.contains("PAYMENT") ? nil : "Finance only"),
            ("Approval review", nil, approver ? nil : "Approver only"),
            ("Add person", .person, owner && allowed.contains("UPDATE") ? nil : "Owner only"),
            ("Role & access", .role, owner && allowed.contains("UPDATE") ? nil : "Owner only"),
            ("Suppliers & channels", .supplier, allowed.contains("UPDATE") ? nil : "View only"),
            ("Invite & QR", .invite, owner && allowed.contains("UPDATE") ? nil : "Owner only"),
        ].filter { matches($0.0) }
        return LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
            ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                Button {
                    if item.0 == "Approval review", item.2 == nil { approvalOpen = true }
                    else if let kind = item.1, item.2 == nil { sheet = kind }
                } label: {
                    VStack(spacing: 8) {
                        Image(item.0 == "Approval review" ? "qa_shield" : (item.1?.icon ?? "qa_shield"))
                            .renderingMode(.template).resizable().scaledToFit().frame(width: 28, height: 28)
                        Text(item.0).font(.plusJakarta(size: 12, weight: .semibold)).multilineTextAlignment(.center)
                        if let lock = item.2 {
                            Text(lock).font(.plusJakarta(size: 10))
                        }
                    }
                    .foregroundStyle(item.2 == nil ? ServiceChrome.ink : ServiceChrome.secondary)
                    .frame(maxWidth: .infinity, minHeight: 104)
                    .padding(12)
                    .background(ServiceChrome.card)
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(ServiceChrome.border, lineWidth: 1))
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                .disabled(item.2 != nil)
            }
        }
    }

    private func matches(_ label: String) -> Bool {
        query.isEmpty || label.localizedCaseInsensitiveContains(query)
    }

    private func chip(_ text: String, filled: Bool) -> some View {
        Text(text)
            .font(.plusJakarta(size: 12))
            .foregroundStyle(filled ? ServiceChrome.bg : ServiceChrome.secondary)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(filled ? ServiceChrome.brand : Color.clear)
            .overlay(Capsule().stroke(filled ? ServiceChrome.brand : ServiceChrome.border, lineWidth: 1))
            .clipShape(Capsule())
    }
}

enum ServiceSheetKind: String, Identifiable {
    case service, appointment, sale, spend, payment, collection, budget, update, issue, reimburse, person, role, invite, supplier
    var id: String { rawValue }
    var label: String {
        switch self {
        case .service: return "Service"
        case .appointment: return "Appointment"
        case .sale: return "Sale"
        case .spend: return "Expense"
        case .payment: return "Payment"
        case .collection: return "Collection"
        case .budget: return "Budget"
        case .update: return "Update"
        case .issue: return "Issue"
        case .reimburse: return "Reimbursement"
        case .person: return "Add person"
        case .role: return "Role & access"
        case .invite: return "Invite & QR"
        case .supplier: return "Add Supplier"
        }
    }
    var icon: String {
        switch self {
        case .service: return "rs_setup_service"
        case .appointment: return "qa_calendar"
        case .sale, .spend: return "qa_receipt"
        case .payment: return "qa_credit_card"
        case .collection: return "qa_arrow_left_right"
        case .budget: return "qa_budget"
        case .update: return "qa_message"
        case .issue: return "qa_alert"
        case .reimburse: return "qa_wallet"
        case .person: return "qa_user_plus"
        case .role: return "qa_users"
        case .invite: return "qa_qr"
        case .supplier: return "qa_network"
        }
    }
    var title: String {
        switch self {
        case .service: return "Record Service"
        case .appointment: return "Appointment / Job"
        case .sale: return "Record Sale"
        case .spend: return "Record Expense"
        case .payment: return "Record Payment"
        case .collection: return "Record Collection"
        case .budget: return "Revise Budget"
        case .update: return "Post Update"
        case .issue: return "Report Issue"
        case .reimburse: return "Record Reimbursement"
        case .person: return "Add person"
        case .role: return "Role & access"
        case .invite: return "Invite & QR"
        case .supplier: return "Add supplier"
        }
    }
    var eventType: String {
        switch self {
        case .service: return "SERVICE"
        case .appointment: return "APPOINTMENT"
        case .sale: return "SALE"
        case .spend: return "SPEND"
        case .payment, .reimburse: return "PAYMENT"
        case .collection: return "COLLECTION"
        case .budget: return "BUDGET"
        case .issue: return "ISSUE"
        default: return "UPDATE"
        }
    }
    var needsAmount: Bool {
        switch self {
        case .appointment, .update, .issue, .person, .role, .invite, .supplier: return false
        default: return true
        }
    }
    var needsChannel: Bool { self == .service || self == .sale || self == .collection }
    var needsParty: Bool { self == .appointment || self == .payment || self == .reimburse || self == .person || self == .role || self == .invite || self == .supplier }
    var paymentStatus: String? {
        switch self {
        case .payment, .collection, .reimburse: return "PENDING"
        default: return nil
        }
    }
    static let core: [ServiceSheetKind] = [.service, .appointment, .sale, .spend, .payment, .collection, .budget, .update, .issue]
}

private struct ServiceEventSheet: View {
    let kind: ServiceSheetKind
    let model: OperatingProjectionPayload
    var onSubmit: (ServiceEventDraft) async throws -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var amount = ""
    @State private var channel = ""
    @State private var party = ""
    @State private var note = ""
    @State private var errorText: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text(kind.title).font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(ServiceChrome.ink)
                    Spacer()
                    Button("Close") { dismiss() }.foregroundStyle(ServiceChrome.secondary)
                }
                Text("\(OperatingMomentCatalog.roles.first { $0.0 == model.role }?.1 ?? "Service") · \(model.title ?? "")")
                    .font(.plusJakarta(size: 12)).foregroundStyle(ServiceChrome.secondary)
                field("Title", text: $title, placeholder: "What happened")
                if kind.needsAmount { field("Amount", text: $amount, placeholder: "0") }
                if kind.needsChannel {
                    Text("Channel").font(.plusJakarta(size: 12)).foregroundStyle(ServiceChrome.secondary)
                    ForEach(model.life?.channels ?? [], id: \.self) { name in
                        Button(name) { channel = name }
                            .foregroundStyle(channel == name ? ServiceChrome.bg : ServiceChrome.secondary)
                    }
                }
                if kind.needsParty { field(kind == .person || kind == .role || kind == .invite ? "Person" : "Name", text: $party, placeholder: "Name") }
                field("Note", text: $note, placeholder: "Optional")
                if let errorText { Text(errorText).foregroundStyle(Color(hex: "#F87171")) }
                Button("Save") { Task { await save() } }
                    .font(.plusJakarta(size: 15, weight: .semibold))
                    .foregroundStyle(ServiceChrome.bg)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(ServiceChrome.brand)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(16)
        }
        .background(ServiceChrome.bg)
        .onAppear { channel = model.life?.channels?.first ?? "" }
    }

    private func field(_ label: String, text: Binding<String>, placeholder: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label).font(.plusJakarta(size: 12)).foregroundStyle(ServiceChrome.secondary)
            TextField(placeholder, text: text)
                .font(.plusJakarta(size: 14))
                .padding(12)
                .background(ServiceChrome.field)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
    }

    private func save() async {
        let trimmed = title.trimmingCharacters(in: .whitespaces).isEmpty ? party.trimmingCharacters(in: .whitespaces) : title.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty { errorText = "A title is required."; return }
        if kind.needsAmount, (Double(amount) ?? 0) <= 0 { errorText = "Amount must be positive."; return }
        do {
            try await onSubmit(ServiceEventDraft(
                eventType: kind.eventType,
                title: trimmed,
                amount: amount.isEmpty ? nil : amount,
                note: note.isEmpty ? nil : note,
                channelName: channel.isEmpty ? nil : channel,
                counterpartyName: party.isEmpty ? nil : party,
                paymentStatus: kind.paymentStatus
            ))
            dismiss()
        } catch {
            errorText = error.localizedDescription
        }
    }
}

private struct ApprovalList: View {
    let model: OperatingProjectionPayload
    var onDecide: (String, String) async throws -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Review Approval").font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(ServiceChrome.ink)
                Spacer()
                Button("Close") { dismiss() }.foregroundStyle(ServiceChrome.secondary)
            }
            let pending = (model.attention ?? []).filter { $0.kind == "APPROVAL" }
            if pending.isEmpty { Text("Nothing is waiting.").foregroundStyle(ServiceChrome.secondary) }
            ForEach(pending) { item in
                Text(item.title).foregroundStyle(ServiceChrome.ink)
                HStack {
                    Button("Approve") { Task { try? await onDecide(item.id, "APPROVED"); dismiss() } }
                        .foregroundStyle(ServiceChrome.success)
                    Button("Reject") { Task { try? await onDecide(item.id, "REJECTED"); dismiss() } }
                        .foregroundStyle(Color(hex: "#F87171"))
                }
            }
            Spacer()
        }
        .padding(16)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(ServiceChrome.bg)
    }
}
