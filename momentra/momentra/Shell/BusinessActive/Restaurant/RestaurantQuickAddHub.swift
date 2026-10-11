import SwiftUI

struct RestaurantQuickAddHub: View {
    let model: OperatingProjectionPayload
    var onRecord: (RestaurantEventDraft) async throws -> Void
    var onDecide: (String, String) async throws -> Void

    @State private var query = ""
    @State private var sheet: RestaurantSheetKind?
    @State private var approvalOpen = false
    @State private var errorText: String?

    private var allowed: Set<String> { Set((model.quickAdds ?? []).map(\.code)) }
    private var owner: Bool { model.role == "OWNER_ADMIN" }
    private var approver: Bool { model.role == "APPROVER" || owner }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Quick Adds").font(.plusJakarta(size: 28, weight: .semibold)).foregroundStyle(RestaurantChrome.ink)
            Text("Record what happened · \(model.title ?? "")").font(.plusJakarta(size: 13)).foregroundStyle(RestaurantChrome.secondary)
            HStack {
                chip(OperatingMomentCatalog.roles.first { $0.0 == model.role }?.1 ?? "Restaurant", filled: true)
                chip("Restaurant", filled: false)
            }
            VStack(alignment: .leading, spacing: 8) {
                Text("Every restaurant moment counts.").font(.plusJakarta(size: 18, weight: .semibold)).foregroundStyle(RestaurantChrome.ink)
                Text("Capture a sale, a cost, or a note while it is still clear.")
                    .font(.plusJakarta(size: 13)).foregroundStyle(RestaurantChrome.secondary)
                if allowed.contains("SALE") {
                    Button("+ Record Sale") { sheet = .sale }
                        .font(.plusJakarta(size: 14, weight: .semibold))
                        .foregroundStyle(RestaurantChrome.bg)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(RestaurantChrome.brand)
                        .clipShape(Capsule())
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(RestaurantChrome.notice)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            TextField("Search actions", text: $query)
                .font(.plusJakarta(size: 14))
                .padding(14)
                .background(RestaurantChrome.field)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            let core = RestaurantSheetKind.core.filter { allowed.contains($0.eventType) && matches($0.label) }
            Text("Core actions · \(core.count)").font(.plusJakarta(size: 12)).foregroundStyle(RestaurantChrome.secondary)
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                ForEach(core) { kind in
                    Button(kind.label) { sheet = kind }
                        .font(.plusJakarta(size: 13, weight: .semibold))
                        .foregroundStyle(RestaurantChrome.ink)
                        .frame(maxWidth: .infinity, minHeight: 72)
                        .background(RestaurantChrome.card)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(RestaurantChrome.border, lineWidth: 1))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }
            }
            Text("Support & management").font(.plusJakarta(size: 12)).foregroundStyle(RestaurantChrome.secondary)
            supportGrid
            if let errorText { Text(errorText).foregroundStyle(Color(hex: "#F87171")) }
        }
        .sheet(item: $sheet) { kind in
            RestaurantEventSheet(kind: kind, model: model) { draft in
                try await onRecord(draft)
            }
        }
        .sheet(isPresented: $approvalOpen) {
            ApprovalList(model: model, onDecide: onDecide)
        }
    }

    private var supportGrid: some View {
        let items: [(String, RestaurantSheetKind?, String?)] = [
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
                    VStack(spacing: 4) {
                        Text(item.0).font(.plusJakarta(size: 12, weight: .semibold))
                        if let lock = item.2 {
                            Text(lock).font(.plusJakarta(size: 10))
                        }
                    }
                    .foregroundStyle(item.2 == nil ? RestaurantChrome.ink : RestaurantChrome.secondary)
                    .frame(maxWidth: .infinity, minHeight: 72)
                    .background(RestaurantChrome.card)
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(RestaurantChrome.border, lineWidth: 1))
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
            .foregroundStyle(filled ? RestaurantChrome.bg : RestaurantChrome.secondary)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(filled ? RestaurantChrome.brand : Color.clear)
            .overlay(Capsule().stroke(filled ? RestaurantChrome.brand : RestaurantChrome.border, lineWidth: 1))
            .clipShape(Capsule())
    }
}

enum RestaurantSheetKind: String, Identifiable {
    case sale, purchase, spend, payment, collection, budget, update, issue, stock, reimburse, person, role, invite, supplier
    var id: String { rawValue }
    var label: String {
        switch self {
        case .sale: return "Sale"
        case .purchase: return "Purchase"
        case .spend: return "Expense"
        case .payment: return "Payment"
        case .collection: return "Collection"
        case .budget: return "Budget"
        case .update: return "Update"
        case .issue: return "Issue"
        case .stock: return "Stock Update"
        case .reimburse: return "Reimbursement"
        case .person: return "Add person"
        case .role: return "Role & access"
        case .invite: return "Invite & QR"
        case .supplier: return "Add Supplier"
        }
    }
    var title: String {
        switch self {
        case .sale: return "Record Sale"
        case .purchase: return "Record Purchase"
        case .spend: return "Record Expense"
        case .payment: return "Record Payment"
        case .collection: return "Record Collection"
        case .budget: return "Revise Budget"
        case .update: return "Post Update"
        case .issue: return "Report Issue"
        case .stock: return "Stock Update"
        case .reimburse: return "Record Reimbursement"
        case .person: return "Add person"
        case .role: return "Role & access"
        case .invite: return "Invite & QR"
        case .supplier: return "Add supplier"
        }
    }
    var eventType: String {
        switch self {
        case .sale: return "SALE"
        case .purchase: return "PURCHASE"
        case .spend: return "SPEND"
        case .payment, .reimburse: return "PAYMENT"
        case .collection: return "COLLECTION"
        case .budget: return "BUDGET"
        case .issue: return "ISSUE"
        case .stock: return "STOCK"
        default: return "UPDATE"
        }
    }
    var needsAmount: Bool {
        switch self {
        case .update, .issue, .stock, .person, .role, .invite, .supplier: return false
        default: return true
        }
    }
    var needsChannel: Bool { self == .sale || self == .collection }
    var needsParty: Bool { self == .purchase || self == .payment || self == .reimburse || self == .person || self == .role || self == .invite || self == .supplier }
    var paymentStatus: String? {
        switch self {
        case .payment, .collection, .reimburse: return "PENDING"
        default: return nil
        }
    }
    static let core: [RestaurantSheetKind] = [.sale, .purchase, .spend, .payment, .collection, .update, .issue, .stock, .supplier]
}

private struct RestaurantEventSheet: View {
    let kind: RestaurantSheetKind
    let model: OperatingProjectionPayload
    var onSubmit: (RestaurantEventDraft) async throws -> Void
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
                    Text(kind.title).font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(RestaurantChrome.ink)
                    Spacer()
                    Button("Close") { dismiss() }.foregroundStyle(RestaurantChrome.secondary)
                }
                Text("\(OperatingMomentCatalog.roles.first { $0.0 == model.role }?.1 ?? "Restaurant") · \(model.title ?? "")")
                    .font(.plusJakarta(size: 12)).foregroundStyle(RestaurantChrome.secondary)
                field("Title", text: $title, placeholder: "What happened")
                if kind.needsAmount { field("Amount", text: $amount, placeholder: "0") }
                if kind.needsChannel {
                    Text("Channel").font(.plusJakarta(size: 12)).foregroundStyle(RestaurantChrome.secondary)
                    ForEach(model.life?.channels ?? [], id: \.self) { name in
                        Button(name) { channel = name }
                            .foregroundStyle(channel == name ? RestaurantChrome.bg : RestaurantChrome.secondary)
                    }
                }
                if kind.needsParty { field(kind == .person || kind == .role || kind == .invite ? "Person" : "Name", text: $party, placeholder: "Name") }
                field("Note", text: $note, placeholder: "Optional")
                if let errorText { Text(errorText).foregroundStyle(Color(hex: "#F87171")) }
                Button("Save") { Task { await save() } }
                    .font(.plusJakarta(size: 15, weight: .semibold))
                    .foregroundStyle(RestaurantChrome.bg)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(RestaurantChrome.brand)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(16)
        }
        .background(RestaurantChrome.bg)
        .onAppear { channel = model.life?.channels?.first ?? "" }
    }

    private func field(_ label: String, text: Binding<String>, placeholder: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label).font(.plusJakarta(size: 12)).foregroundStyle(RestaurantChrome.secondary)
            TextField(placeholder, text: text)
                .font(.plusJakarta(size: 14))
                .padding(12)
                .background(RestaurantChrome.field)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
    }

    private func save() async {
        let trimmed = title.trimmingCharacters(in: .whitespaces).isEmpty ? party.trimmingCharacters(in: .whitespaces) : title.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty { errorText = "A title is required."; return }
        if kind.needsAmount, (Double(amount) ?? 0) <= 0 { errorText = "Amount must be positive."; return }
        do {
            try await onSubmit(RestaurantEventDraft(
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
                Text("Review Approval").font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(RestaurantChrome.ink)
                Spacer()
                Button("Close") { dismiss() }.foregroundStyle(RestaurantChrome.secondary)
            }
            let pending = (model.attention ?? []).filter { $0.kind == "APPROVAL" }
            if pending.isEmpty { Text("Nothing is waiting.").foregroundStyle(RestaurantChrome.secondary) }
            ForEach(pending) { item in
                Text(item.title).foregroundStyle(RestaurantChrome.ink)
                HStack {
                    Button("Approve") { Task { try? await onDecide(item.id, "APPROVED"); dismiss() } }
                        .foregroundStyle(RestaurantChrome.success)
                    Button("Reject") { Task { try? await onDecide(item.id, "REJECTED"); dismiss() } }
                        .foregroundStyle(Color(hex: "#F87171"))
                }
            }
            Spacer()
        }
        .padding(16)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(RestaurantChrome.bg)
    }
}
