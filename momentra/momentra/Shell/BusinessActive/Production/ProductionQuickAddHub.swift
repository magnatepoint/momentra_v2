import SwiftUI

struct ProductionQuickAddHub: View {
    let model: OperatingProjectionPayload
    var onRecord: (ProductionEventDraft) async throws -> Void
    var onDecide: (String, String) async throws -> Void

    @State private var query = ""
    @State private var sheet: ProductionSheetKind?
    @State private var approvalOpen = false
    @State private var errorText: String?
    @Environment(\.productionChrome) private var chrome

    private var allowed: Set<String> { Set((model.quickAdds ?? []).map(\.code)) }
    private var owner: Bool { model.role == "OWNER_ADMIN" }
    private var approver: Bool { model.role == "APPROVER" || owner }
    private var job: Bool { model.subtype == "MADE_TO_ORDER" }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Quick Adds").font(.plusJakarta(size: 28, weight: .semibold)).foregroundStyle(chrome.ink)
            Text("Record what happened · \(model.title ?? "")").font(.plusJakarta(size: 13)).foregroundStyle(chrome.secondary)
            HStack {
                chip(OperatingMomentCatalog.roles.first { $0.0 == model.role }?.1 ?? "Production", filled: true)
                chip(job ? "Job work" : "Standard", filled: false)
            }
            VStack(alignment: .leading, spacing: 8) {
                Text(job ? "Order. Produce. Deliver. Collect." : "Produce. Dispatch. Collect.")
                    .font(.plusJakarta(size: 18, weight: .semibold))
                    .foregroundStyle(chrome.ink)
                Text(job
                     ? "Capture customer orders, materials, production and delivery. Orders are expectations, not automatic revenue."
                     : "Capture batches, raw materials, buyer commitments and linked money in one factory Moment.")
                    .font(.plusJakarta(size: 13))
                    .foregroundStyle(chrome.secondary)
                if job ? allowed.contains("CUSTOMER_ORDER") : allowed.contains("PRODUCTION") {
                    Button { sheet = job ? .customerOrder : .production } label: {
                        HStack(spacing: 6) {
                            Image("qa_plus").renderingMode(.template).resizable().scaledToFit().frame(width: 14, height: 14)
                            Text(job ? "Record Customer Order" : "Record production")
                        }
                    }
                    .font(.plusJakarta(size: 14, weight: .semibold))
                    .foregroundStyle(job ? Color(hex: "#66C7A4") : Color(hex: "#2563EB"))
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(job ? Color(hex: "#EFF5F2") : Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(chrome.card)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            HStack(spacing: 10) {
                Image("qa_search").renderingMode(.template).resizable().scaledToFit().frame(width: 18, height: 18)
                    .foregroundStyle(chrome.ink)
                TextField("Search actions", text: $query).font(.plusJakarta(size: 14))
            }
            .padding(14)
            .background(chrome.field)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            let core = ProductionSheetKind.core.filter { allowed.contains($0.eventType) && matches($0.label) }
            Text("Core actions · \(core.count)").font(.plusJakarta(size: 12)).foregroundStyle(chrome.secondary)
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                ForEach(core) { kind in
                    Button { sheet = kind } label: {
                        VStack(spacing: 8) {
                            Image(kind.icon).renderingMode(.template).resizable().scaledToFit().frame(width: 28, height: 28)
                            Text(kind.label).font(.plusJakarta(size: 13, weight: .semibold)).multilineTextAlignment(.center)
                        }
                        .foregroundStyle(chrome.ink)
                        .frame(maxWidth: .infinity, minHeight: 104)
                        .padding(12)
                        .background(chrome.card)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(chrome.border, lineWidth: 1))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                    }
                }
            }
            Text("Support & management").font(.plusJakarta(size: 12)).foregroundStyle(chrome.secondary)
            supportGrid
            if approver {
                Button("Review approvals") { approvalOpen = true }
                    .font(.plusJakarta(size: 15, weight: .semibold))
                    .foregroundStyle(chrome.accent)
            }
            if let errorText { Text(errorText).foregroundStyle(Color(hex: "#F87171")) }
        }
        .sheet(item: $sheet) { kind in
            ProductionEventSheet(kind: kind, model: model) { draft in
                try await onRecord(draft)
            }
        }
        .sheet(isPresented: $approvalOpen) {
            ApprovalList(model: model, onDecide: onDecide)
        }
    }

    private var supportGrid: some View {
        let items: [(String, ProductionSheetKind?, String?)] = [
            ("Reimbursement", .reimburse, nil),
            ("Approval review", .approval, approver ? nil : "Approver only"),
            ("Add person", .person, owner ? nil : "Owner only"),
            ("Role & access", .role, owner ? nil : "Owner only"),
            (job ? "Suppliers / customers" : "Suppliers / buyers", .partner, owner ? nil : "Owner only"),
            ("Invite & QR", .invite, owner ? nil : "Owner only"),
        ].filter { matches($0.0) }
        return LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
            ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                Button {
                    if item.0 == "Approval review", item.2 == nil { approvalOpen = true }
                    else if let kind = item.1, item.2 == nil { sheet = kind }
                } label: {
                    VStack(spacing: 8) {
                        Image(item.1?.icon ?? "qa_shield")
                            .renderingMode(.template).resizable().scaledToFit().frame(width: 28, height: 28)
                        Text(item.0).font(.plusJakarta(size: 12, weight: .semibold)).multilineTextAlignment(.center)
                        if let lock = item.2 {
                            Text(lock).font(.plusJakarta(size: 10))
                        }
                    }
                    .foregroundStyle(item.2 == nil ? chrome.ink : chrome.secondary)
                    .frame(maxWidth: .infinity, minHeight: 104)
                    .padding(12)
                    .background(chrome.card)
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(chrome.border, lineWidth: 1))
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
            .foregroundStyle(filled ? chrome.bg : chrome.secondary)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(filled ? chrome.brand : Color.clear)
            .overlay(Capsule().stroke(filled ? chrome.brand : chrome.border, lineWidth: 1))
            .clipShape(Capsule())
    }
}

enum ProductionSheetKind: String, Identifiable {
    case production, purchase, spend, sale, customerOrder, delivery, payment, collection, budget, target, update, issue, stock, reimburse, approval, person, role, partner, invite
    var id: String { rawValue }
    var label: String {
        switch self {
        case .production: return "Production"
        case .purchase: return "Material purchase"
        case .spend: return "Expense"
        case .sale: return "Sale / dispatch"
        case .customerOrder: return "Customer order"
        case .delivery: return "Delivery"
        case .payment: return "Payment"
        case .collection: return "Collection"
        case .budget: return "Budget / target"
        case .target: return "Target"
        case .update: return "Update"
        case .issue: return "Issue"
        case .stock: return "Material / stock"
        case .reimburse: return "Reimbursement"
        case .approval: return "Approval review"
        case .person: return "Add person"
        case .role: return "Role & access"
        case .partner: return "Suppliers"
        case .invite: return "Invite & QR"
        }
    }
    var icon: String {
        switch self {
        case .production: return "qa_factory"
        case .purchase: return "qa_wallet"
        case .spend, .sale: return "qa_receipt"
        case .payment: return "qa_credit_card"
        case .collection: return "qa_arrow_left_right"
        case .budget, .target: return "qa_budget"
        case .update: return "qa_message"
        case .issue: return "qa_alert"
        case .customerOrder, .stock: return "qa_clipboard"
        case .delivery: return "qa_truck"
        case .reimburse: return "qa_receipt"
        case .approval: return "qa_shield"
        case .person: return "qa_user_plus"
        case .role: return "qa_users"
        case .partner: return "qa_network"
        case .invite: return "qa_qr"
        }
    }
    var title: String { label }
    var eventType: String {
        switch self {
        case .production: return "PRODUCTION"
        case .purchase: return "PURCHASE"
        case .spend: return "SPEND"
        case .sale: return "SALE"
        case .customerOrder: return "CUSTOMER_ORDER"
        case .delivery: return "DELIVERY"
        case .payment: return "PAYMENT"
        case .collection: return "COLLECTION"
        case .budget: return "BUDGET"
        case .target: return "TARGET"
        case .issue: return "ISSUE"
        case .stock: return "STOCK"
        case .reimburse: return "SPEND"
        case .approval: return "APPROVAL"
        default: return "UPDATE"
        }
    }
    var needsAmount: Bool {
        switch self {
        case .production, .purchase, .spend, .sale, .customerOrder, .payment, .collection, .budget, .target, .reimburse: return true
        default: return false
        }
    }
    var needsChannel: Bool { false }
    var needsParty: Bool {
        switch self {
        case .purchase, .sale, .customerOrder, .delivery, .payment, .person, .role, .partner: return true
        default: return false
        }
    }
    var paymentStatus: String? {
        switch self {
        case .payment, .collection, .customerOrder: return "PENDING"
        default: return nil
        }
    }
    static let core: [ProductionSheetKind] = [.customerOrder, .production, .purchase, .spend, .sale, .delivery, .payment, .collection, .budget, .update, .issue, .stock]
}

private struct ProductionEventSheet: View {
    let kind: ProductionSheetKind
    let model: OperatingProjectionPayload
    var onSubmit: (ProductionEventDraft) async throws -> Void
    @Environment(\.dismiss) private var dismiss
    @Environment(\.productionChrome) private var chrome
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
                    Text(kind.title).font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(chrome.ink)
                    Spacer()
                    Button("Close") { dismiss() }.foregroundStyle(chrome.secondary)
                }
                Text("\(OperatingMomentCatalog.roles.first { $0.0 == model.role }?.1 ?? "Production") · \(model.title ?? "")")
                    .font(.plusJakarta(size: 12)).foregroundStyle(chrome.secondary)
                field("Title", text: $title, placeholder: "What happened")
                if kind.needsAmount { field("Amount", text: $amount, placeholder: "0") }
                if kind.needsChannel {
                    Text("Channel").font(.plusJakarta(size: 12)).foregroundStyle(chrome.secondary)
                    ForEach(model.life?.channels ?? [], id: \.self) { name in
                        Button(name) { channel = name }
                            .foregroundStyle(channel == name ? chrome.bg : chrome.secondary)
                    }
                }
                if kind.needsParty { field(kind == .person || kind == .role || kind == .invite ? "Person" : "Name", text: $party, placeholder: "Name") }
                field("Note", text: $note, placeholder: "Optional")
                if let errorText { Text(errorText).foregroundStyle(Color(hex: "#F87171")) }
                Button("Save") { Task { await save() } }
                    .font(.plusJakarta(size: 15, weight: .semibold))
                    .foregroundStyle(chrome.bg)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(chrome.brand)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(16)
        }
        .background(chrome.bg)
        .onAppear { channel = model.life?.channels?.first ?? "" }
    }

    private func field(_ label: String, text: Binding<String>, placeholder: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label).font(.plusJakarta(size: 12)).foregroundStyle(chrome.secondary)
            TextField(placeholder, text: text)
                .font(.plusJakarta(size: 14))
                .padding(12)
                .background(chrome.field)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
    }

    private func save() async {
        let trimmed = title.trimmingCharacters(in: .whitespaces).isEmpty ? party.trimmingCharacters(in: .whitespaces) : title.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty { errorText = "A title is required."; return }
        if kind.needsAmount, (Double(amount) ?? 0) <= 0 { errorText = "Amount must be positive."; return }
        do {
            try await onSubmit(ProductionEventDraft(
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
    @Environment(\.productionChrome) private var chrome

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Review Approval").font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(chrome.ink)
                Spacer()
                Button("Close") { dismiss() }.foregroundStyle(chrome.secondary)
            }
            let pending = (model.attention ?? []).filter { $0.kind == "APPROVAL" }
            if pending.isEmpty { Text("Nothing is waiting.").foregroundStyle(chrome.secondary) }
            ForEach(pending) { item in
                Text(item.title).foregroundStyle(chrome.ink)
                HStack {
                    Button("Approve") { Task { try? await onDecide(item.id, "APPROVED"); dismiss() } }
                        .foregroundStyle(chrome.success)
                    Button("Reject") { Task { try? await onDecide(item.id, "REJECTED"); dismiss() } }
                        .foregroundStyle(Color(hex: "#F87171"))
                }
            }
            Spacer()
        }
        .padding(16)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(chrome.bg)
    }
}
