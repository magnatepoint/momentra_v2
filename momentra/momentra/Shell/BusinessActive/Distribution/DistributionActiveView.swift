import SwiftUI

struct DistributionActiveView: View {
    let model: OperatingProjectionPayload
    let tab: OperatingTab
    var onOpenQuickAdd: () -> Void = {}
    var onRecord: (DistributionEventDraft) async throws -> Void = { _ in }
    var onDecide: (String, String) async throws -> Void = { _, _ in }

    @State private var activityOpen = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                switch tab {
                case .pulse: pulse
                case .moments: moments
                case .life: life
                case .memory: memory
                case .quickAdd: DistributionQuickAddHub(model: model, onRecord: onRecord, onDecide: onDecide)
                }
            }
            .padding(.horizontal, 17)
            .padding(.vertical, 20)
        }
        .background(DistributionChrome.bg)
        .sheet(isPresented: $activityOpen) { activitySheet }
    }

    private var pulse: some View {
        VStack(alignment: .leading, spacing: 16) {
            header("Pulse", "Your sales plan, distribution commitments and ready money.")
            if model.financeVisible == true {
                sectionTitle("Target & Budget")
                card {
                    Text("Recorded sales").font(.plusJakarta(size: 12)).foregroundStyle(DistributionChrome.secondary)
                    Text(money(model.target?.progress)).font(.plusJakarta(size: 28, weight: .semibold)).foregroundStyle(DistributionChrome.ink)
                    Text("of \(money(model.target?.value)) sales target").font(.plusJakarta(size: 13)).foregroundStyle(DistributionChrome.secondary)
                    meter("Sales progress", fraction(model.target?.progress, model.target?.value), DistributionChrome.brand)
                    meter("Budget used", fraction(model.budget?.spent, model.budget?.amount), DistributionChrome.success)
                    line("Budget remaining", money(model.budget?.remaining), DistributionChrome.success)
                }
                sectionTitle("Needs Attention")
                if (model.attention ?? []).isEmpty {
                    card { Text("Nothing is waiting.").foregroundStyle(DistributionChrome.secondary) }
                }
                ForEach(model.attention ?? []) { item in
                    card {
                        Text(item.kind).font(.plusJakarta(size: 11, weight: .semibold)).foregroundStyle(DistributionChrome.accent)
                        Text(item.title).foregroundStyle(DistributionChrome.ink)
                    }
                }
                sectionTitle("Payment Readiness")
                readiness("Ready", (model.paymentReadiness ?? []).filter { $0.status == "READY" })
                readiness("Expected", (model.paymentReadiness ?? []).filter { $0.status == "PENDING" })
                readiness("Not ready", (model.paymentReadiness ?? []).filter { $0.status != "READY" && $0.status != "PENDING" })
                sectionTitle("Finance")
                card {
                    line("Recorded sales", money(model.finance?.sales), DistributionChrome.accent)
                    line("Received", money(model.finance?.received), DistributionChrome.success)
                    line("Outstanding", money(model.finance?.outstanding), Color(hex: "#FBBF24"))
                    line("Eligible cost", money(model.finance?.eligibleCost), DistributionChrome.ink)
                    line("Business cash paid", money(model.finance?.cashPaid), DistributionChrome.ink)
                }
            } else {
                card {
                    Text("Finance totals stay with Finance, Approver, and Owner.")
                        .foregroundStyle(DistributionChrome.ink)
                }
                ForEach(model.attention ?? []) { item in
                    card { Text(item.title).foregroundStyle(DistributionChrome.ink) }
                }
            }
            sectionTitle("Activity")
            ForEach(Array((model.activity ?? []).prefix(5))) { row in activityRow(row) }
            if (model.activity ?? []).isEmpty {
                card { Text("Nothing recorded yet.").foregroundStyle(DistributionChrome.secondary) }
            }
            Button("View all activity") { activityOpen = true }
                .font(.plusJakarta(size: 15, weight: .semibold))
                .foregroundStyle(DistributionChrome.accent)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(DistributionChrome.brand, lineWidth: 1))
        }
    }

    private var moments: some View {
        VStack(alignment: .leading, spacing: 16) {
            header("Moments", "Dispatches, stock, and the distribution this month.")
            card {
                Text(period).font(.plusJakarta(size: 12)).foregroundStyle(DistributionChrome.accent)
                Text("This month, in motion").font(.plusJakarta(size: 22, weight: .semibold)).foregroundStyle(DistributionChrome.ink)
                HStack {
                    metric("Sales", money(model.finance?.sales ?? model.target?.progress))
                    metric("Target", money(model.target?.value))
                }
                HStack {
                    metric("Spent", money(model.budget?.spent))
                    metric("Remaining", money(model.budget?.remaining))
                }
                meter("Sales toward target", fraction(model.target?.progress, model.target?.value), DistributionChrome.brand)
            }
            ForEach(model.boards?.moments ?? [], id: \.code) { section in
                board(section)
            }
            card {
                Text("Create the next distribution moment").font(.plusJakarta(size: 16, weight: .semibold)).foregroundStyle(DistributionChrome.ink)
                Button("Open Quick Add", action: onOpenQuickAdd)
                    .font(.plusJakarta(size: 14, weight: .semibold))
                    .foregroundStyle(DistributionChrome.bg)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(DistributionChrome.brand)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }

    private var life: some View {
        VStack(alignment: .leading, spacing: 16) {
            header("Life", "How the distribution is moving today.")
            ForEach(model.boards?.life ?? []) { board($0) }
            if (model.boards?.life ?? []).isEmpty {
                card { Text("Nothing recorded yet.").foregroundStyle(DistributionChrome.secondary) }
            }
        }
    }

    private var memory: some View {
        VStack(alignment: .leading, spacing: 16) {
            header("Memory", "What this distribution keeps from the month.")
            HStack {
                pill(period)
                pill("Current")
            }
            ForEach(model.boards?.memory ?? []) { board($0) }
        }
    }

    private var activitySheet: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("All activity").font(.plusJakarta(size: 22, weight: .semibold)).foregroundStyle(DistributionChrome.ink)
                Text(model.title ?? "Distribution").foregroundStyle(DistributionChrome.secondary)
                ForEach(model.activity ?? []) { activityRow($0) }
                if (model.activity ?? []).isEmpty {
                    Text("Nothing recorded yet.").foregroundStyle(DistributionChrome.secondary)
                }
            }
            .padding(18)
        }
        .background(DistributionChrome.bg)
    }

    private func header(_ title: String, _ body: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(title).font(.plusJakarta(size: 32, weight: .semibold)).foregroundStyle(DistributionChrome.ink)
                Spacer()
                pill(roleLabel(model.role))
            }
            Text(body).font(.plusJakarta(size: 14)).foregroundStyle(DistributionChrome.secondary)
            Text(snapshot).font(.plusJakarta(size: 12)).foregroundStyle(DistributionChrome.secondary)
        }
    }

    private func board(_ section: OperatingBoardSectionPayload) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            sectionTitle(section.title)
            card {
                Text(section.detail.isEmpty ? "Nothing recorded yet." : section.detail)
                    .font(.plusJakarta(size: 12)).foregroundStyle(DistributionChrome.secondary)
                ForEach(section.rows ?? []) { row in
                    VStack(alignment: .leading, spacing: 2) {
                        Text(row.title).foregroundStyle(DistributionChrome.ink)
                        let extra = [row.channelName, row.counterpartyName, row.paymentStatus].compactMap { $0 }.filter { $0 != "NONE" }
                        if !extra.isEmpty {
                            Text(extra.joined(separator: " · ")).font(.plusJakarta(size: 12)).foregroundStyle(DistributionChrome.secondary)
                        }
                        if model.financeVisible == true, let amount = row.amount, !amount.isEmpty {
                            Text(amount).font(.plusJakarta(size: 13, weight: .semibold)).foregroundStyle(DistributionChrome.accent)
                        }
                    }
                }
            }
        }
    }

    private func activityRow(_ row: OperatingActivityPayload) -> some View {
        card {
            Text(row.eventType.replacingOccurrences(of: "_", with: " "))
                .font(.plusJakarta(size: 11)).foregroundStyle(DistributionChrome.accent)
            Text(row.title).foregroundStyle(DistributionChrome.ink)
            let bits = [row.channelName, row.counterpartyName, model.financeVisible == true ? row.amount : nil].compactMap { $0 }
            if !bits.isEmpty {
                Text(bits.joined(separator: " · ")).font(.plusJakarta(size: 12)).foregroundStyle(DistributionChrome.secondary)
            }
        }
    }

    @ViewBuilder private func readiness(_ label: String, _ rows: [OperatingPaymentPayload]) -> some View {
        if !rows.isEmpty {
            card {
                Text(label).font(.plusJakarta(size: 12, weight: .semibold)).foregroundStyle(DistributionChrome.accent)
                ForEach(rows) { item in
                    line(item.title, money(item.amount), DistributionChrome.ink)
                }
            }
        }
    }

    private func card<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 8, content: content)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(DistributionChrome.card)
            .overlay(RoundedRectangle(cornerRadius: 18).stroke(DistributionChrome.border, lineWidth: 1))
            .clipShape(RoundedRectangle(cornerRadius: 18))
    }

    private func sectionTitle(_ text: String) -> some View {
        Text(text).font(.plusJakarta(size: 20, weight: .semibold)).foregroundStyle(DistributionChrome.ink)
    }

    private func pill(_ text: String) -> some View {
        Text(text)
            .font(.plusJakarta(size: 11, weight: .semibold))
            .foregroundStyle(DistributionChrome.accent)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(DistributionChrome.notice)
            .clipShape(Capsule())
    }

    private func metric(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label).font(.plusJakarta(size: 11)).foregroundStyle(DistributionChrome.secondary)
            Text(value).font(.plusJakarta(size: 16, weight: .semibold)).foregroundStyle(DistributionChrome.ink)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(DistributionChrome.notice)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func meter(_ label: String, _ fraction: Double, _ color: Color) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(label).font(.plusJakarta(size: 12)).foregroundStyle(DistributionChrome.secondary)
                Spacer()
                Text("\(Int(fraction * 100))%").font(.plusJakarta(size: 12)).foregroundStyle(color)
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule().fill(DistributionChrome.border)
                    Capsule().fill(color).frame(width: geo.size.width * min(max(fraction, 0), 1))
                }
            }
            .frame(height: 6)
        }
    }

    private func line(_ label: String, _ value: String, _ color: Color) -> some View {
        HStack {
            Text(label).foregroundStyle(DistributionChrome.ink)
            Spacer()
            Text(value).font(.plusJakarta(size: 14, weight: .semibold)).foregroundStyle(color)
        }
    }

    private var period: String {
        let raw = model.startDate ?? ""
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd"
        let date = formatter.date(from: String(raw.prefix(10))) ?? Date()
        formatter.dateFormat = "MMMM yyyy"
        return formatter.string(from: date)
    }

    private var snapshot: String {
        let today = DateFormatter()
        today.locale = Locale(identifier: "en_US_POSIX")
        today.dateFormat = "dd MMM yyyy"
        let from = model.startDate?.prefix(10).description
        return "Snapshot · from \(from?.isEmpty == false ? from! : today.string(from: Date())) · \(today.string(from: Date()))"
    }

    private func money(_ raw: String?) -> String {
        guard model.financeVisible == true, let raw, !raw.isEmpty else { return "—" }
        let number = raw.split(separator: ".").first.map(String.init) ?? raw
        return (model.currency ?? "INR") == "INR" ? "₹\(number)" : "\(model.currency ?? "") \(number)"
    }

    private func fraction(_ part: String?, _ whole: String?) -> Double {
        let p = Double(part ?? "") ?? 0
        let w = Double(whole ?? "") ?? 0
        return w > 0 ? p / w : 0
    }

    private func roleLabel(_ code: String?) -> String {
        OperatingMomentCatalog.roles.first { $0.0 == code }?.1 ?? "Distribution"
    }
}

enum DistributionChrome {
    static let bg = Color(hex: "#071014")
    static let card = Color(hex: "#0C171B")
    static let notice = Color(hex: "#15262C")
    static let field = Color(hex: "#0C171B")
    static let border = Color(hex: "#345058")
    static let brand = Color(hex: "#66C7A4")
    static let accent = Color(hex: "#66C7A4")
    static let secondary = Color(hex: "#66C7A4")
    static let ink = Color(hex: "#EFF5F2")
    static let success = Color(hex: "#66C7A4")
}

struct DistributionEventDraft {
    var eventType: String
    var title: String
    var amount: String?
    var note: String?
    var channelName: String?
    var counterpartyName: String?
    var paymentStatus: String?
}
