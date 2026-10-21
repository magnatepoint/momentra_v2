import Combine
import Foundation

@MainActor
final class OperatingChromeState: ObservableObject {
    static let shared = OperatingChromeState()
    @Published var participantNames: [String] = []
    @Published var editing: OperatingActivityPayload?
    @Published var settling: OperatingPaymentPayload?
}

func formatOperatingAmount(_ raw: String?, currency: String?, visible: Bool) -> String {
    guard visible, let raw, !raw.isEmpty, let value = Decimal(string: raw) else { return "—" }
    let format = NumberFormatter()
    format.numberStyle = .decimal
    format.locale = Locale(identifier: "en_IN")
    format.maximumFractionDigits = 2
    let text = format.string(from: value as NSDecimalNumber) ?? raw
    let code = (currency?.isEmpty == false ? currency! : "INR")
    return code == "INR" ? "₹\(text)" : "\(code) \(text)"
}

func formatOperatingDetail(_ detail: String, currency: String?) -> String {
    guard let regex = try? NSRegularExpression(pattern: #"\d+\.\d{4}(?:\s+[A-Z]{3})?"#) else { return detail }
    let range = NSRange(detail.startIndex..., in: detail)
    var result = detail
    for match in regex.matches(in: detail, range: range).reversed() {
        guard let span = Range(match.range, in: detail) else { continue }
        let token = String(detail[span]).split(separator: " ").first.map(String.init) ?? ""
        let formatted = formatOperatingAmount(token, currency: currency, visible: true)
        result.replaceSubrange(span, with: formatted)
    }
    return result
}
